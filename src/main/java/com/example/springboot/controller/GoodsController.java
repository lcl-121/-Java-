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
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private IGoodsService goodsService;
    @Resource
    private ICollectService collectService;
    @Resource
    private IOrdersService ordersService;
    @Resource
    private IUserService userService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Goods goods) {
        if(goods.getId()==null){
            goods.setUnitId(TokenUtils.getCurrentUser().getId());
            goods.setDate(DateUtil.today());
            goods.setSales(0);
        }

        return Result.success(goodsService.saveOrUpdate(goods));
    }

    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(goodsService.removeById(id));
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(goodsService.removeByIds(ids));
    }

    //查询所有
    @AuthAccess
    @GetMapping
    public Result findAll() {
        return Result.success(goodsService.list());
    }

    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        Goods goods = goodsService.getById(id);

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,TokenUtils.getCurrentUser().getId());
        wrapper.eq(Collect::getItemId,id);
        Collect one = collectService.getOne(wrapper);

        goods.setIsCollected(one != null);

        return Result.success(goods);
    }

    @GetMapping("/front/page")
    public Result findFrontPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "0") Integer typeId,
                           @RequestParam(defaultValue = "综合") String  sort,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();

        if(typeId!=0){
            queryWrapper.eq(Goods::getTypeId,typeId);
        }

        if(StrUtil.equals(sort,"综合")){
            queryWrapper.orderByAsc(Goods::getId);
        }else if(StrUtil.equals(sort,"销量")){
            queryWrapper.orderByDesc(Goods::getSales);
        }else if(StrUtil.equals(sort,"最新")){
            queryWrapper.orderByAsc(Goods::getDate);
        }

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/unit/page")
    public Result findUnitPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam Integer unitId,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Goods::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        queryWrapper.eq(Goods::getUnitId,unitId);

        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/collect/page")
    public Result findCollectPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        Account account = TokenUtils.getCurrentUser();

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,account.getId());
        List<Collect> collectList = collectService.list(wrapper);
        //我们拿到了当前用户的收藏信息，但我们只要的是商品id
        if(CollectionUtil.isEmpty(collectList)){
            return Result.success(collectService.page(new Page<>(pageNum, pageSize), wrapper));
        }
        //用户收藏不为空
        List<Integer> ids = new ArrayList<>();

        for (Collect collect : collectList) {
            ids.add(collect.getItemId());
        }

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Goods::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        queryWrapper.in(Goods::getId, ids);

        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Goods::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        Account account = TokenUtils.getCurrentUser();
        if(!StrUtil.equals(account.getRole(),"ROLE_ADMIN")){
            queryWrapper.eq(Goods::getUnitId,account.getId());
        }

        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @AuthAccess
    @GetMapping("/recommend")
    public Result recommend() {
        //推荐物品数量
        int RECOMMENDER_NUM = 4;
        //获取当前用户信息
        Account currentUser = TokenUtils.getCurrentUser();
        //如果当前用户没有登录，直接随机返回4个
        if (ObjectUtil.isNull(currentUser)){
            List<Goods> allItems = goodsService.list();
            return Result.success(getRandomItems(RECOMMENDER_NUM,allItems));
        }
        // 获取所有的收藏信息
        List<Collect> allCollects = collectService.list();
        // 获取所有下单过的商品
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Orders::getStatus, "已取消");
        List<Orders> allOrders = ordersService.list(wrapper);
        // 获取所有的用户信息

        List<User> allUsers = userService.list();
        // 获取所有的物品信息
        List<Goods> allItems = goodsService.list();

        // 定义一个存储每个物品和每个用户关系的List
        List<RelateDTO> data = new ArrayList<>();

        // 开始计算每个物品和每个用户之间的关系数据
        for (Goods item : allItems) {
            Integer itemId = item.getId();
            for (User user : allUsers) {
                Integer userId = user.getId();
                int index = 1;
                // 判断该用户有没有收藏过，收藏的权重给 1
                Optional<Collect> collectOptional = allCollects.stream().filter(x -> x.getItemId().equals(itemId) && x.getUserId().equals(userId)).findFirst();
                if (collectOptional.isPresent()) {
                    index += 1;
                }
                // 判断该用户有没有下单过，下单的权重给 3
                Optional<Orders> ordersOptional = allOrders.stream().filter(x -> x.getGoodsId().equals(itemId) && x.getUserId().equals(userId)).findFirst();
                if (ordersOptional.isPresent()) {
                    index += 3;
                }
                if (index > 1) {
                    RelateDTO relateDTO = new RelateDTO(userId, itemId, index);
                    data.add(relateDTO);
                }
            }
        }

        // 数据准备结束后，就把这些数据(数据集)一起喂给这个推荐算法
        List<Integer> itemIds = UserCF.recommend(currentUser.getId(), data);
        // 把物品id转换成物品
        List<Goods> recommendResult = itemIds.stream().map(itemId -> allItems.stream()
                        .filter(x -> x.getId().equals(itemId)).findFirst().orElse(null))
                .limit(RECOMMENDER_NUM).collect(Collectors.toList());
        // 如果数据集不足或者数据不准确,也可能导致能推荐出的物品不足
        if (recommendResult.size() < RECOMMENDER_NUM) {
            // 还需要被推荐的物品数num
            int num = RECOMMENDER_NUM - recommendResult.size();
            // 计算所有物品和已经推荐物品的差集 (即没有被推荐过的物品)
            List<Goods> items = allItems.stream()
                    .filter(item -> !recommendResult.contains(item))
                    .collect(Collectors.toList());
            // 从差集中随机获取num个不重复的数据
            List<Goods> list = getRandomItems(num,items);
            // 把随机得到的数据也放到推荐结果中
            recommendResult.addAll(list);
            return Result.success(recommendResult);
        }
        return Result.success(recommendResult);
    }

    private <T> List<T> getRandomItems(int num,List<T> items) {
        List<T> list = new ArrayList<>();
        // 用set保证数组下标不重复
        Set<Integer> indexes = new HashSet<>();
        // 循环获得不重复的下标
        while (indexes.size() < num && indexes.size() < items.size()) {
            int index = new Random().nextInt(items.size());
            if (indexes.add(index)){
                // 如果添加成功,说明这个下标之前不存在
                // 通过对应下标取出对应内容
                list.add(items.get(index));
            }
        }
        return list;
    }

}

