package com.example.springboot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.*;
import com.example.springboot.service.*;
import com.example.springboot.utils.RelateDTO;
import com.example.springboot.utils.TokenUtils;
import com.example.springboot.utils.UserCF;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品模块 前端控制器
 * 处理商品（Goods）相关的HTTP请求，提供增删改查、分页查询、收藏状态查询、商品推荐等接口
 * </p>
 */
@RestController
@RequestMapping("/goods") // 该控制器的基础请求路径，所有接口均以/goods开头
public class GoodsController {

    // 注入商品业务层服务对象，用于调用商品相关的业务逻辑
    @Resource
    private IGoodsService goodsService;
    // 注入收藏业务层服务对象，用于调用收藏相关的业务逻辑
    @Resource
    private ICollectService collectService;
    // 注入订单业务层服务对象，用于调用订单相关的业务逻辑
    @Resource
    private IOrdersService ordersService;
    // 注入用户业务层服务对象，用于调用用户相关的业务逻辑
    @Resource
    private IUserService userService;

    /**
     * 新增/更新商品
     * 新增时自动填充店铺ID、创建日期、初始销量；更新时直接复用原有逻辑
     * @param goods 商品实体对象（接收前端传递的JSON格式参数）
     * @return 统一返回结果Result，包含新增/更新是否成功的布尔值
     */
    @PostMapping
    public Result save(@RequestBody Goods goods) {
        // 新增商品时（ID为空）执行初始化逻辑
        if(goods.getId()==null){
            // 关联当前登录用户（商家）的ID作为店铺ID
            goods.setUnitId(TokenUtils.getCurrentUser().getId());
            // 设置商品创建日期为当天
            goods.setDate(DateUtil.today());
            // 初始化商品销量为0
            goods.setSales(0);
        }
        // 执行新增或更新操作并返回结果
        return Result.success(goodsService.saveOrUpdate(goods));
    }

    /**
     * 根据ID删除单个商品
     * @param id 商品主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含删除是否成功的布尔值
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(goodsService.removeById(id));
    }

    /**
     * 批量删除商品
     * @param ids 商品主键ID集合（接收前端传递的ID列表）
     * @return 统一返回结果Result，包含批量删除是否成功的布尔值
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(goodsService.removeByIds(ids));
    }

    /**
     * 查询所有商品（无需登录即可访问）
     * @return 统一返回结果Result，包含所有商品的列表
     */
    @AuthAccess // 自定义注解：标识该接口无需登录即可访问
    @GetMapping
    public Result findAll() {
        return Result.success(goodsService.list());
    }

    /**
     * 根据ID查询单个商品详情（包含当前用户的收藏状态）
     * @param id 商品主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含带收藏状态的商品实体对象
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        // 根据ID查询商品基础信息
        Goods goods = goodsService.getById(id);

        // 构建收藏查询条件：当前用户 + 当前商品
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,TokenUtils.getCurrentUser().getId());
        wrapper.eq(Collect::getItemId,id);
        // 查询当前用户是否收藏了该商品
        Collect one = collectService.getOne(wrapper);

        // 为商品对象设置收藏状态（true：已收藏，false：未收藏）
        goods.setIsCollected(one != null);

        // 返回带收藏状态的商品信息
        return Result.success(goods);
    }

    /**
     * 前端商品分页查询（支持分类筛选、排序、关键词搜索）
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页显示条数（必填）
     * @param typeId 商品分类ID（默认0：不筛选分类）
     * @param sort 排序方式（默认"综合"，支持"销量"/"最新"）
     * @param keyword 搜索关键词（默认空字符串：不搜索）
     * @return 统一返回结果Result，包含分页后的商品列表及分页信息
     */
    @GetMapping("/front/page")
    public Result findFrontPage(@RequestParam Integer pageNum,
                                @RequestParam Integer pageSize,
                                @RequestParam(defaultValue = "0") Integer typeId,
                                @RequestParam(defaultValue = "综合") String  sort,
                                @RequestParam(defaultValue = "") String keyword) {

        // 构建商品查询条件构造器
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();

        // 分类筛选：typeId不为0时，按分类ID筛选商品
        if(typeId!=0){
            queryWrapper.eq(Goods::getTypeId,typeId);
        }

        // 排序逻辑：根据前端传入的排序类型设置排序规则
        if(StrUtil.equals(sort,"综合")){
            // 综合排序：按商品ID升序
            queryWrapper.orderByAsc(Goods::getId);
        }else if(StrUtil.equals(sort,"销量")){
            // 销量排序：按销量降序
            queryWrapper.orderByDesc(Goods::getSales);
        }else if(StrUtil.equals(sort,"最新")){
            // 最新排序：按创建日期升序（日期越新越靠前）
            queryWrapper.orderByAsc(Goods::getDate);
        }

        // 关键词搜索：关键词非空时，按商品名称模糊查询
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        // 执行分页查询并返回结果
        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 按店铺ID分页查询商品（支持关键词搜索）
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页显示条数（必填）
     * @param unitId 店铺ID（必填，用于筛选指定店铺的商品）
     * @param keyword 搜索关键词（默认空字符串：不搜索）
     * @return 统一返回结果Result，包含指定店铺的商品分页列表
     */
    @GetMapping("/unit/page")
    public Result findUnitPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam Integer unitId,
                               @RequestParam(defaultValue = "") String keyword) {

        // 构建商品查询条件构造器
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        // 默认按商品ID降序排序
        queryWrapper.orderByDesc(Goods::getId);

        // 关键词搜索：关键词非空时，按商品名称模糊查询
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        // 筛选指定店铺的商品
        queryWrapper.eq(Goods::getUnitId,unitId);

        // 执行分页查询并返回结果
        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 分页查询当前用户的收藏商品（支持关键词搜索）
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页显示条数（必填）
     * @param keyword 搜索关键词（默认空字符串：不搜索）
     * @return 统一返回结果Result，包含用户收藏商品的分页列表
     */
    @GetMapping("/collect/page")
    public Result findCollectPage(@RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize,
                                  @RequestParam(defaultValue = "") String keyword) {

        // 获取当前登录用户信息
        Account account = TokenUtils.getCurrentUser();

        // 构建收藏查询条件：当前用户的所有收藏记录
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,account.getId());
        // 查询当前用户的所有收藏记录
        List<Collect> collectList = collectService.list(wrapper);

        // 若用户无收藏记录，返回空分页结果
        if(CollectionUtil.isEmpty(collectList)){
            return Result.success(collectService.page(new Page<>(pageNum, pageSize), wrapper));
        }

        // 提取收藏记录中的商品ID列表
        List<Integer> ids = new ArrayList<>();
        for (Collect collect : collectList) {
            ids.add(collect.getItemId());
        }

        // 构建商品查询条件构造器
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        // 默认按商品ID降序排序
        queryWrapper.orderByDesc(Goods::getId);

        // 关键词搜索：关键词非空时，按商品名称模糊查询
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        // 筛选用户收藏的商品（根据收藏的商品ID列表）
        queryWrapper.in(Goods::getId, ids);

        // 执行分页查询并返回结果
        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 商品分页查询（管理员查全部，商家仅查自己店铺的商品，支持关键词搜索）
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页显示条数（必填）
     * @param keyword 搜索关键词（默认空字符串：不搜索）
     * @return 统一返回结果Result，包含商品分页列表及分页信息
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 构建商品查询条件构造器
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        // 默认按商品ID降序排序
        queryWrapper.orderByDesc(Goods::getId);

        // 关键词搜索：关键词非空时，按商品名称模糊查询
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        // 获取当前登录用户信息
        Account account = TokenUtils.getCurrentUser();
        // 权限控制：非管理员用户仅查询自己店铺（unitId）的商品
        if(!StrUtil.equals(account.getRole(),"ROLE_ADMIN")){
            queryWrapper.eq(Goods::getUnitId,account.getId());
        }

        // 执行分页查询并返回结果
        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 商品推荐接口（基于用户协同过滤算法，无需登录也可访问）
     * 登录用户：根据收藏/下单行为推荐；未登录用户：随机推荐4个商品
     * @return 统一返回结果Result，包含4个推荐商品的列表
     */
    @AuthAccess // 自定义注解：标识该接口无需登录即可访问
    @GetMapping("/recommend")
    public Result recommend() {
        // 推荐商品的固定数量
        int RECOMMENDER_NUM = 4;
        // 获取当前登录用户信息
        Account currentUser = TokenUtils.getCurrentUser();

        // 未登录用户：直接随机返回4个商品
        if (ObjectUtil.isNull(currentUser)){
            List<Goods> allItems = goodsService.list();
            return Result.success(getRandomItems(RECOMMENDER_NUM,allItems));
        }

        // 已登录用户：基于用户行为（收藏/下单）进行推荐
        // 1. 获取所有收藏记录
        List<Collect> allCollects = collectService.list();
        // 2. 获取所有非取消状态的订单记录（过滤已取消订单）
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Orders::getStatus, "已取消");
        List<Orders> allOrders = ordersService.list(wrapper);
        // 3. 获取所有用户信息
        List<User> allUsers = userService.list();
        // 4. 获取所有商品信息
        List<Goods> allItems = goodsService.list();

        // 定义存储「用户-商品-关联权重」的数据集
        List<RelateDTO> data = new ArrayList<>();

        // 计算每个用户对每个商品的关联权重（基础权重1，收藏+1，下单+3）
        for (Goods item : allItems) {
            Integer itemId = item.getId();
            for (User user : allUsers) {
                Integer userId = user.getId();
                int index = 1; // 基础权重

                // 收藏权重：用户收藏该商品则+1
                Optional<Collect> collectOptional = allCollects.stream()
                        .filter(x -> x.getItemId().equals(itemId) && x.getUserId().equals(userId))
                        .findFirst();
                if (collectOptional.isPresent()) {
                    index += 1;
                }

                // 下单权重：用户下单该商品则+3
                Optional<Orders> ordersOptional = allOrders.stream()
                        .filter(x -> x.getGoodsId().equals(itemId) && x.getUserId().equals(userId))
                        .findFirst();
                if (ordersOptional.isPresent()) {
                    index += 3;
                }

                // 关联权重>1时，加入数据集（说明用户有过收藏/下单行为）
                if (index > 1) {
                    RelateDTO relateDTO = new RelateDTO(userId, itemId, index);
                    data.add(relateDTO);
                }
            }
        }

        // 调用用户协同过滤算法，获取推荐商品ID列表
        List<Integer> itemIds = UserCF.recommend(currentUser.getId(), data);

        // 将推荐商品ID转换为商品对象，并限制最多4个
        List<Goods> recommendResult = itemIds.stream()
                .map(itemId -> allItems.stream()
                        .filter(x -> x.getId().equals(itemId))
                        .findFirst().orElse(null))
                .limit(RECOMMENDER_NUM)
                .collect(Collectors.toList());

        // 若推荐结果不足4个，补充随机商品
        if (recommendResult.size() < RECOMMENDER_NUM) {
            // 计算需要补充的商品数量
            int num = RECOMMENDER_NUM - recommendResult.size();
            // 筛选出未被推荐的商品（差集）
            List<Goods> items = allItems.stream()
                    .filter(item -> !recommendResult.contains(item))
                    .collect(Collectors.toList());
            // 从差集中随机获取num个商品
            List<Goods> list = getRandomItems(num,items);
            // 补充到推荐结果中
            recommendResult.addAll(list);
            return Result.success(recommendResult);
        }

        // 返回最终推荐结果
        return Result.success(recommendResult);
    }

    /**
     * 从指定列表中随机获取指定数量的不重复元素
     * @param num 需要获取的元素数量
     * @param items 源列表
     * @return 随机筛选后的元素列表（数量≤num且≤源列表长度）
     */
    private <T> List<T> getRandomItems(int num,List<T> items) {
        List<T> list = new ArrayList<>();
        // 使用Set保证随机下标不重复
        Set<Integer> indexes = new HashSet<>();

        // 循环生成不重复的随机下标，直到达到指定数量或源列表长度
        while (indexes.size() < num && indexes.size() < items.size()) {
            // 生成0到源列表长度-1的随机下标
            int index = new Random().nextInt(items.size());
            // 若下标未重复，则添加对应元素到结果列表
            if (indexes.add(index)){
                list.add(items.get(index));
            }
        }
        return list;
    }

}