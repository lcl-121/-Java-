package com.example.springboot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Cart;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.User;
import com.example.springboot.service.ICartService;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车模块 前端控制器
 * 处理购物车（Cart）相关的HTTP请求，提供增删改查、分页查询、修改商品数量等接口
 * </p>
 */
@RestController
@RequestMapping("/cart") // 该控制器的基础请求路径，所有接口均以/cart开头
public class CartController {

    // 注入购物车业务层服务对象，用于调用购物车相关的业务逻辑
    @Resource
    private ICartService cartService;
    // 注入商品业务层服务对象，用于调用商品相关的业务逻辑
    @Resource
    private IGoodsService goodsService;

    /**
     * 新增/更新购物车商品
     * 新增时校验库存，若购物车已存在该商品则累加数量，更新时直接修改
     * @param cart 购物车实体对象（接收前端传递的JSON格式参数）
     * @return 统一返回结果Result，包含操作是否成功的布尔值；库存不足时返回错误提示
     */
    @PostMapping
    public Result save(@RequestBody Cart cart) {
        // 新增购物车商品时（ID为空）执行库存校验和用户关联逻辑
        if (cart.getId() == null) {
            // 根据商品ID查询商品信息
            Goods goods = goodsService.getById(cart.getGoodsId());
            // 校验库存是否充足，不足则返回错误结果
            if (goods.getInventory()-cart.getNum()<0){
                return Result.error("605","库存不足，无法加购！");
            }
            // 获取当前登录用户信息
            Account account = TokenUtils.getCurrentUser();
            // 根据用户ID和商品ID查询购物车中是否已存在该商品
            Cart one = cartService.getOne(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, account.getId()).eq(Cart::getGoodsId, cart.getGoodsId()));
            // 检查购物车中是否已存在该商品
            if (one != null) {
                // 如果存在，累加商品数量
                int newNum = one.getNum() + cart.getNum();
                one.setNum(newNum);
                cart = one;
            }
            // 关联当前用户ID到购物车对象
            cart.setUserId(account.getId());
        }
        // 执行新增或更新操作并返回结果
        return Result.success(cartService.saveOrUpdate(cart));
    }

    /**
     * 修改购物车商品数量
     * @param cart 购物车实体对象（至少包含ID和新数量）
     * @return 统一返回结果Result，标识操作成功
     */
    @PostMapping("/num")
    public Result changNum(@RequestBody Cart cart) {
        // 根据ID更新购物车商品数量
        cartService.updateById(cart);
        return Result.success();
    }

    /**
     * 根据ID删除单个购物车商品
     * @param id 购物车主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含删除是否成功的布尔值
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(cartService.removeById(id));
    }

    /**
     * 批量删除购物车商品
     * @param ids 购物车主键ID集合（接收前端传递的ID列表）
     * @return 统一返回结果Result，包含批量删除是否成功的布尔值
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(cartService.removeByIds(ids));
    }

    /**
     * 查询当前登录用户的所有购物车商品（关联商品详情）
     * 自动拼接商品名称、价格、图片、库存等信息到购物车对象中
     * @return 统一返回结果Result，包含带商品详情的购物车列表
     */
    @GetMapping
    public Result findAll() {
        // 构建查询条件：仅查询当前登录用户的购物车商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId,TokenUtils.getCurrentUser().getId());
        // 查询当前用户的所有购物车商品
        List<Cart> cartList = cartService.list(wrapper);
        // 提取购物车中所有商品的ID，用于批量查询商品详情
        List<Integer> goodsIds = cartList.stream()
                .map(Cart::getGoodsId)
                .collect(Collectors.toList());
        // 若购物车无商品，直接返回空列表
        if (CollectionUtil.isEmpty(goodsIds)) return Result.success(new ArrayList<>());
        // 根据商品ID批量查询商品详情
        List<Goods> goodsList = goodsService.listByIds(goodsIds);

        // 遍历购物车列表，为每个购物车项补充商品详情
        for (Cart cart : cartList) {
            // 根据商品ID从商品列表中匹配对应的商品信息
            Goods goods = goodsList.stream()
                    .filter(g -> g.getId().equals(cart.getGoodsId()))
                    .findFirst()
                    .orElse(null);

            // 若商品信息存在，将商品属性赋值到购物车对象中
            if (goods != null) {
                cart.setGoodsName(goods.getName());
                cart.setGoodsPrice(goods.getPrice());
                cart.setGoodsImg(goods.getImg());
                cart.setGoodsInfo(goods.getInfo());
                cart.setGoodsInventory(goods.getInventory());
            }
        }
        // 返回带商品详情的购物车列表
        return Result.success(cartList);
    }

    /**
     * 根据ID查询单个购物车商品详情
     * @param id 购物车主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含该ID对应的购物车实体对象
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(cartService.getById(id));
    }

    /**
     * 购物车商品分页查询（支持商品ID模糊搜索）
     * @param pageNum 当前页码（前端传递，必填）
     * @param pageSize 每页显示条数（前端传递，必填）
     * @param keyword 搜索关键词（默认空字符串，根据商品ID模糊查询）
     * @return 统一返回结果Result，包含分页查询后的购物车列表及分页信息
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 构建MyBatis-Plus的Lambda查询条件构造器
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：按购物车ID降序排列
        queryWrapper.orderByDesc(Cart::getId);

        // 如果关键词非空，则添加商品ID的模糊查询条件
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Cart::getGoodsId, keyword);
        }

        // 执行分页查询并返回结果
        return Result.success(cartService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}