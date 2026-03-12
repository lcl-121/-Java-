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

/**
 * <p>
 * ECharts数据统计模块 前端控制器
 * 处理ECharts可视化图表所需的统计数据请求，提供商品分类销量、商品销售额TOP10等统计接口
 * </p>
 */
@RestController
@RequestMapping("/echarts") // 该控制器的基础请求路径，所有接口均以/echarts开头
public class EchartsController {

    // 注入订单业务层服务对象，用于调用订单相关的业务逻辑
    @Resource
    private IOrdersService ordersService;
    // 注入商品业务层服务对象，用于调用商品相关的业务逻辑
    @Resource
    private IGoodsService goodsService;
    // 注入商品分类业务层服务对象，用于调用分类相关的业务逻辑
    @Resource
    private ITypeService typeService;

    /**
     * 统计各商品分类的销量数据（用于ECharts图表展示）
     * 管理员可查看全平台数据，普通商家仅查看自身相关数据
     * @return 统一返回结果Result，包含各分类名称及对应销量的JSON数组
     */
    @GetMapping("/count1")
    public Result count1() {
        // 构建订单查询条件构造器
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        // 获取当前登录用户信息
        Account account = TokenUtils.getCurrentUser();
        // 权限控制：非管理员用户仅查询自己店铺（unitId）的订单数据
        if (!StrUtil.equals(account.getRole(), "ROLE_ADMIN")) {
            wrapper.eq(Orders::getUnitId, account.getId());
        }

        // 根据条件查询订单列表
        List<Orders> orders = ordersService.list(wrapper);

        // 构建分类ID与分类名称的映射关系（key：分类ID，value：分类名称）
        Map<Integer, String> typeMap = typeService.list().stream().collect(Collectors.toMap(Type::getId, Type::getName));

        // 构建分类ID与对应商品列表的映射关系（key：分类ID，value：该分类下的商品列表）
        Map<Integer, List<Goods>> typeGoodsMap = goodsService.list().stream().collect(Collectors.groupingBy(Goods::getTypeId));

        // 统计每个商品ID对应的销量（key：商品ID，value：销量）
        Map<Integer, Long> salesCountByType = orders.stream().collect(Collectors.groupingBy(Orders::getGoodsId, Collectors.counting()));

        // 初始化JSON数组，用于封装最终返回的分类销量数据
        JSONArray array = new JSONArray();

        // 遍历所有商品分类，计算每个分类的总销量
        for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
            Integer typeId = entry.getKey();       // 当前分类ID
            String typeName = entry.getValue();    // 当前分类名称
            List<Goods> goodsList = typeGoodsMap.get(typeId); // 当前分类下的所有商品

            long count = 0; // 初始化当前分类的总销量

            // 若该分类下有商品，则累加所有商品的销量
            if (CollectionUtil.isNotEmpty(goodsList)){
                for (Goods goods : goodsList) {
                    // 累加当前商品的销量（无销量则默认0）
                    count += salesCountByType.getOrDefault(goods.getId(), 0L);
                }
            }

            // 封装当前分类的销量数据到JSON对象
            JSONObject object = new JSONObject();
            object.set("name", typeName); // 分类名称
            object.set("value", count);   // 分类总销量
            array.add(object);            // 将当前分类数据加入JSON数组
        }

        // 返回封装好的分类销量数据
        return Result.success(array);
    }

    /**
     * 统计销售额TOP10的商品数据（用于ECharts图表展示）
     * 管理员可查看全平台数据，普通商家仅查看自身相关数据
     * @return 统一返回结果Result，包含TOP10商品名称及对应销售额的JSON数组
     */
    @GetMapping("/count2")
    public Result count2() {
        // 构建订单查询条件构造器
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        // 获取当前登录用户信息
        Account account = TokenUtils.getCurrentUser();
        // 权限控制：非管理员用户仅查询自己店铺（unitId）的订单数据
        if (!StrUtil.equals(account.getRole(), "ROLE_ADMIN")) {
            wrapper.eq(Orders::getUnitId, account.getId());
        }

        // 根据条件查询订单列表
        List<Orders> orders = ordersService.list(wrapper);

        // 构建商品ID与商品对象的映射关系（key：商品ID，value：商品对象）
        Map<Integer, Goods> goodsMap = goodsService.list().stream().collect(Collectors.toMap(Goods::getId, g -> g));

        // 初始化Map，用于统计每个商品的销售额（key：商品ID，value：销售额）
        Map<Integer, BigDecimal> salesAmountByProduct = new HashMap<>();

        // 遍历订单列表，计算每个商品的销售额
        for (Orders order : orders) {
            Goods goods = goodsMap.get(order.getGoodsId()); // 根据商品ID获取商品信息
            if (goods != null) {
                // 计算当前订单中该商品的销售额（商品单价 * 购买数量）
                BigDecimal amount = goods.getPrice().multiply(BigDecimal.valueOf(order.getNum()));
                // 累加该商品的总销售额（无销售额则默认0，再加上当前订单销售额）
                salesAmountByProduct.put(order.getGoodsId(), salesAmountByProduct.getOrDefault(order.getGoodsId(), BigDecimal.ZERO).add(amount));
            }
        }

        // 按销售额降序排序，筛选出销售额前10的商品
        List<Map.Entry<Integer, BigDecimal>> top10Products = salesAmountByProduct.entrySet().stream()
                .sorted(Map.Entry.<Integer, BigDecimal>comparingByValue().reversed()) // 按销售额降序
                .limit(10) // 取前10条
                .collect(Collectors.toList());

        // 初始化JSON数组，用于封装最终返回的TOP10商品销售额数据
        JSONArray array = new JSONArray();

        // 遍历TOP10商品，封装数据到JSON对象
        for (Map.Entry<Integer, BigDecimal> entry : top10Products) {
            Integer productId = entry.getKey();          // 商品ID
            BigDecimal salesAmount = entry.getValue();   // 商品销售额
            String productName = goodsMap.get(productId).getName(); // 商品名称

            // 封装当前商品的销售额数据到JSON对象
            JSONObject object = new JSONObject();
            object.set("name", productName);    // 商品名称
            object.set("value", salesAmount);   // 商品销售额
            array.add(object);                  // 将当前商品数据加入JSON数组
        }

        // 返回封装好的TOP10商品销售额数据
        return Result.success(array);
    }

}