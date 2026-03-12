package com.example.springboot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.Type;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.service.ITypeService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/echarts")
public class EchartsController {

    @Resource
    private IOrdersService ordersService;
    @Resource
    private IGoodsService goodsService;
    @Resource
    private ITypeService typeService;


    @GetMapping("/count1")
    public Result count1() {

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        Account account = TokenUtils.getCurrentUser();
        //不是管理员的话，只显示商家自己的信息
        if (!StrUtil.equals(account.getRole(), "ROLE_ADMIN")) {
            wrapper.eq(Orders::getUnitId, account.getId());
        }

        // 查询订单列表
        List<Orders> orders = ordersService.list(wrapper);

        // 分类map
        Map<Integer, String> typeMap = typeService.list().stream().collect(Collectors.toMap(Type::getId, Type::getName));

        // 分类商品map
        Map<Integer, List<Goods>> typeGoodsMap = goodsService.list().stream().collect(Collectors.groupingBy(Goods::getTypeId));

        // 按分类统计销量
        Map<Integer, Long> salesCountByType = orders.stream().collect(Collectors.groupingBy(Orders::getGoodsId, Collectors.counting()));

        JSONArray array = new JSONArray();

        // 遍历分类map，构建结果
        for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
            Integer typeId = entry.getKey();
            String typeName = entry.getValue();

            List<Goods> goodsList = typeGoodsMap.get(typeId);

            long count = 0;

            if (CollectionUtil.isNotEmpty(goodsList)){
                for (Goods goods : goodsList) {
                    count+=salesCountByType.getOrDefault(goods.getId(), 0L); // 获取销量，默认值为0
                }
            }

            JSONObject object = new JSONObject();
            object.set("name", typeName);
            object.set("value", count);
            array.add(object);
        }


        return Result.success(array);
    }

    @GetMapping("/count2")
    public Result count2() {

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        Account account = TokenUtils.getCurrentUser();
        // 不是管理员的话，只显示商家自己的信息
        if (!StrUtil.equals(account.getRole(), "ROLE_ADMIN")) {
            wrapper.eq(Orders::getUnitId, account.getId());
        }

        // 查询订单列表
        List<Orders> orders = ordersService.list(wrapper);

        // 商品map
        Map<Integer, Goods> goodsMap = goodsService.list().stream().collect(Collectors.toMap(Goods::getId, g -> g));

        // 计算每个商品的销售额
        Map<Integer, BigDecimal> salesAmountByProduct = new HashMap<>();

        for (Orders order : orders) {
            Goods goods = goodsMap.get(order.getGoodsId());
            if (goods != null) {
                BigDecimal amount = goods.getPrice().multiply(BigDecimal.valueOf(order.getNum())); // 假设 order 里有 num 属性表示数量
                salesAmountByProduct.put(order.getGoodsId(), salesAmountByProduct.getOrDefault(order.getGoodsId(), BigDecimal.ZERO).add(amount));
            }
        }

        // 获取销售额前 10 的商品
        List<Map.Entry<Integer, BigDecimal>> top10Products = salesAmountByProduct.entrySet().stream().sorted(Map.Entry.<Integer, BigDecimal>comparingByValue().reversed()).limit(10).collect(Collectors.toList());

        JSONArray array = new JSONArray();

        // 构建结果
        for (Map.Entry<Integer, BigDecimal> entry : top10Products) {
            Integer productId = entry.getKey();
            BigDecimal salesAmount = entry.getValue();
            String productName = goodsMap.get(productId).getName(); // 获取商品名称

            JSONObject object = new JSONObject();
            object.set("name", productName);
            object.set("value", salesAmount); // 设置销售额
            array.add(object);
        }

        return Result.success(array);
    }

}
