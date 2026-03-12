package com.example.springboot.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.*;
import com.example.springboot.service.IAddressService;
import com.example.springboot.service.ICartService;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 订单模块 前端控制器
 * 处理订单（Orders）相关的HTTP请求，包含订单创建、状态修改、分页查询、删除等核心接口
 * </p>
 */
@RestController
@RequestMapping("/orders") // 该控制器的基础请求路径，所有接口均以/orders开头
public class OrdersController {

    // 注入订单业务层服务对象
    @Resource
    private IOrdersService ordersService;
    // 注入商品业务层服务对象
    @Resource
    private IGoodsService goodsService;
    // 注入地址业务层服务对象
    @Resource
    private IAddressService addressService;
    // 注入购物车业务层服务对象
    @Resource
    private ICartService cartService;

    /**
     * 新增/更新订单（单个商品下单）
     * 新增时会校验商品库存，生成订单编号，计算订单金额，扣减商品库存并增加销量
     * 更新时仅执行订单信息更新，不处理库存相关逻辑
     * @param orders 订单实体对象（接收前端传递的JSON参数）
     * @return 统一返回结果Result，包含新增/更新是否成功的布尔值
     */
    @PostMapping
    @Transactional // 开启事务，确保库存修改和订单操作原子性
    public Result save(@RequestBody Orders orders) {
        // 判断是否为新增订单（ID为空则为新增）
        if (orders.getId() == null) {
            // 根据商品ID查询商品信息
            Goods goods = goodsService.getById(orders.getGoodsId());
            // 校验商品库存是否充足
            if (goods.getInventory()-orders.getNum()<0){
                return Result.error("605","库存不足，无法下单！");
            }
            // 生成订单编号：当前时间（yyyyMMddHHmmss）+6位随机数
            orders.setNo(DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6));
            // 设置订单所属用户ID（从当前登录用户Token中获取）
            orders.setUserId(TokenUtils.getCurrentUser().getId());
            // 设置订单商品名称（取自商品表）
            orders.setName(goods.getName());
            // 计算订单总金额：购买数量 * 商品单价
            orders.setPrice(new BigDecimal(orders.getNum()).multiply(goods.getPrice()));
            // 设置订单创建时间（当前时间）
            orders.setTime(DateUtil.now());
            // 设置订单商品图片（取自商品表）
            orders.setImg(goods.getImg());
            // 设置订单初始状态：待支付
            orders.setStatus("待支付");
            // 根据地址ID查询收货地址信息
            Address address = addressService.getById(orders.getAddressId());
            // 设置订单收货人姓名
            orders.setUserName(address.getName());
            // 设置订单收货人地址
            orders.setUserAddress(address.getAddress());
            // 设置订单收货人电话
            orders.setUserPhone(address.getPhone());
            // 扣减商品库存
            goods.setInventory(goods.getInventory()-orders.getNum());
            // 增加商品销量
            goods.setSales(goods.getSales()+orders.getNum());
            // 更新商品库存和销量
            goodsService.updateById(goods);
        }
        // 执行订单新增或更新操作并返回结果
        return Result.success(ordersService.saveOrUpdate(orders));
    }

    /**
     * 从购物车批量创建订单
     * 遍历购物车商品，逐个创建订单，扣减库存，增加销量，并删除购物车记录
     * @param carts 购物车商品列表（接收前端传递的JSON数组）
     * @param addressId 收货地址ID（从URL路径中获取）
     * @return 统一返回结果Result，标识批量下单是否成功
     */
    @PostMapping("/fromCart/{addressId}")
    @Transactional // 开启事务，确保所有订单操作原子性（要么全成功，要么全回滚）
    public Result fromCart(@RequestBody List<Cart> carts, @PathVariable Integer addressId) {
        // 遍历购物车列表，逐个处理下单逻辑
        for (Cart cart : carts) {
            // 创建新订单对象
            Orders order = new Orders();
            // 设置订单商品ID（取自购物车）
            order.setGoodsId(cart.getGoodsId());
            // 设置订单购买数量（取自购物车）
            order.setNum(cart.getNum());
            // 设置订单所属用户ID（从当前登录用户Token中获取）
            order.setUserId(TokenUtils.getCurrentUser().getId());
            // 设置订单创建时间（当前时间）
            order.setTime(DateUtil.now());
            // 根据商品ID查询商品信息
            Goods goods = goodsService.getById(cart.getGoodsId());
            // 检查商品库存是否充足
            if (goods.getInventory() - order.getNum() < 0) {
                return Result.error("605","商品库存不足");
            }
            // 扣减商品库存
            goods.setInventory(goods.getInventory()-order.getNum());
            // 增加商品销量
            goods.setSales(goods.getSales()+order.getNum());
            // 更新商品库存和销量
            goodsService.updateById(goods);
            // 设置订单商品图片（取自商品表）
            order.setImg(goods.getImg());
            // 设置订单商品名称（取自商品表）
            order.setName(goods.getName());
            // 计算订单总金额：购买数量 * 商品单价
            order.setPrice(new BigDecimal(cart.getNum()).multiply(goods.getPrice()));;
            // 设置订单初始状态：待支付
            order.setStatus("待支付");
            // 设置订单所属商家ID（取自商品表）
            order.setUnitId(goods.getUnitId());
            // 根据地址ID查询收货地址信息
            Address address = addressService.getById(addressId);
            // 生成订单编号：当前时间（yyyyMMddHHmmss）+6位随机数
            order.setNo(DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6));
            // 设置订单收货人姓名
            order.setUserName(address.getName());
            // 设置订单收货人电话
            order.setUserPhone(address.getPhone());
            // 设置订单收货人地址
            order.setUserAddress(address.getAddress());
            // 保存订单信息
            ordersService.save(order);
            // 删除当前购物车记录（下单后从购物车移除）
            cartService.removeById(cart);
        }
        // 批量下单成功，返回成功结果
        return Result.success();
    }

    /**
     * 申请退款（将订单状态改为待退款）
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，标识操作是否成功
     */
    @GetMapping("/back/{id}")
    public Result back(@PathVariable Integer id) {
        // 根据ID查询订单信息
        Orders orders = ordersService.getById(id);
        // 修改订单状态为：待退款
        orders.setStatus("待退款");
        // 更新订单状态
        ordersService.updateById(orders);
        // 返回成功结果
        return Result.success();
    }

    /**
     * 确认退款（将订单状态改为已退款）
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，标识操作是否成功
     */
    @GetMapping("/ok/{id}")
    public Result ok(@PathVariable Integer id) {
        // 根据ID查询订单信息
        Orders orders = ordersService.getById(id);
        // 修改订单状态为：已退款
        orders.setStatus("已退款");
        // 更新订单状态
        ordersService.updateById(orders);
        // 返回成功结果
        return Result.success();
    }

    /**
     * 拒绝退款/完成收货（将订单状态改为待评价）
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，标识操作是否成功
     */
    @GetMapping("/no/{id}")
    public Result no(@PathVariable Integer id) {
        // 根据ID查询订单信息
        Orders orders = ordersService.getById(id);
        // 修改订单状态为：待评价
        orders.setStatus("待评价");
        // 更新订单状态
        ordersService.updateById(orders);
        // 返回成功结果
        return Result.success();
    }

    /**
     * 订单支付（将订单状态改为待发货）
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，标识操作是否成功
     */
    @GetMapping("/pay/{id}")
    public Result pay(@PathVariable Integer id) {
        // 根据ID查询订单信息
        Orders orders = ordersService.getById(id);
        // 修改订单状态为：待发货
        orders.setStatus("待发货");
        // 更新订单状态
        ordersService.updateById(orders);
        // 返回成功结果
        return Result.success();
    }

    /**
     * 取消订单（将订单状态改为已取消，并恢复商品库存）
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，标识操作是否成功
     */
    @GetMapping("/cancel/{id}")
    @Transactional // 开启事务，确保订单状态修改和库存恢复原子性
    public Result cancel(@PathVariable Integer id) {
        // 根据ID查询订单信息
        Orders orders = ordersService.getById(id);
        // 修改订单状态为：已取消
        orders.setStatus("已取消");
        // 根据订单商品ID查询商品信息
        Goods goods = goodsService.getById(orders.getGoodsId());
        // 恢复商品库存（将取消订单的商品数量加回库存）
        goods.setInventory(goods.getInventory()+orders.getNum());
        // 更新商品库存
        goodsService.updateById(goods);
        // 更新订单状态
        ordersService.updateById(orders);
        // 返回成功结果
        return Result.success();
    }

    /**
     * 商家发货（将订单状态改为待收货）
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，标识操作是否成功
     */
    @GetMapping("/send/{id}")
    public Result send(@PathVariable Integer id) {
        // 根据ID查询订单信息
        Orders orders = ordersService.getById(id);
        // 修改订单状态为：待收货
        orders.setStatus("待收货");
        // 更新订单状态
        ordersService.updateById(orders);
        // 返回成功结果
        return Result.success();
    }

    /**
     * 用户确认收货（将订单状态改为待评价）
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，标识操作是否成功
     */
    @GetMapping("/delivery/{id}")
    public Result delivery(@PathVariable Integer id) {
        // 根据ID查询订单信息
        Orders orders = ordersService.getById(id);
        // 修改订单状态为：待评价
        orders.setStatus("待评价");
        // 更新订单状态
        ordersService.updateById(orders);
        // 返回成功结果
        return Result.success();
    }

    /**
     * 查询指定商品的已完成订单（用于商品评价，无需登录即可访问）
     * @param id 商品主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含该商品的已完成订单列表
     */
    @AuthAccess // 自定义注解：标识该接口无需登录即可访问
    @GetMapping("/goodComment/{id}")
    public Result findGoodComment(@PathVariable Integer id) {
        // 构建订单查询条件构造器
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        // 条件：商品ID等于传入的ID
        wrapper.eq(Orders::getGoodsId,id);
        // 条件：订单状态为“已完成”
        wrapper.eq(Orders::getStatus,"已完成");
        // 执行查询并返回结果
        return Result.success(ordersService.list(wrapper));
    }

    /**
     * 根据ID删除单个订单
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含删除是否成功的布尔值
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(ordersService.removeById(id));
    }

    /**
     * 批量删除订单
     * @param ids 订单主键ID集合（接收前端传递的ID列表）
     * @return 统一返回结果Result，包含批量删除是否成功的布尔值
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(ordersService.removeByIds(ids));
    }

    /**
     * 查询所有订单（管理员/商家视角）
     * @return 统一返回结果Result，包含所有订单的列表
     */
    @GetMapping
    public Result findAll() {
        return Result.success(ordersService.list());
    }

    /**
     * 根据ID查询单个订单详情
     * @param id 订单主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含单个订单的实体对象
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(ordersService.getById(id));
    }

    /**
     * 前端用户订单分页查询（按用户ID、关键词、订单状态过滤）
     * @param keyword 搜索关键词（默认空字符串：不搜索，匹配商品名称）
     * @param activeTab 订单状态标签（默认空字符串：所有订单，匹配订单状态）
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页显示条数（必填）
     * @return 统一返回结果Result，包含分页后的用户订单列表及分页信息
     */
    @GetMapping("/front/page")
    public Result findFrontPage(@RequestParam(defaultValue = "") String keyword,
                                @RequestParam(defaultValue = "") String activeTab,
                                @RequestParam Integer pageNum,
                                @RequestParam Integer pageSize) {
        // 构建订单查询条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 默认按订单ID降序排序（最新创建的订单排在前面）
        queryWrapper.orderByDesc(Orders::getId);
        // 条件：订单所属用户ID等于当前登录用户ID
        queryWrapper.eq(Orders::getUserId, TokenUtils.getCurrentUser().getId());

        // 关键词搜索：关键词非空时，按商品名称模糊查询
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Orders::getName, keyword);
        }
        // 根据订单状态标签过滤：非“所有订单”时，匹配订单状态
        if (!StrUtil.equals(activeTab,"所有订单")){
            queryWrapper.eq(Orders::getStatus,activeTab);
        }
        // 执行分页查询并返回结果
        return Result.success(ordersService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 后台订单分页查询（管理员/商家视角，按订单编号搜索，商家仅看自己的订单）
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页显示条数（必填）
     * @param keyword 搜索关键词（默认空字符串：不搜索，匹配订单编号）
     * @return 统一返回结果Result，包含分页后的订单列表及分页信息
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {
        // 构建订单查询条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 默认按订单ID降序排序（最新创建的订单排在前面）
        queryWrapper.orderByDesc(Orders::getId);

        // 关键词搜索：关键词非空时，按订单编号模糊查询
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Orders::getNo, keyword);
        }

        // 获取当前登录用户信息
        Account account = TokenUtils.getCurrentUser();
        // 非管理员角色：仅显示当前商家（unitId）的订单
        if (!StrUtil.equals(account.getRole(),"ROLE_ADMIN")){
            queryWrapper.eq(Orders::getUnitId,account.getId());
        }

        // 执行分页查询并返回结果
        return Result.success(ordersService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}