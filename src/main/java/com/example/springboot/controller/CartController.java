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
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Resource
    private ICartService cartService;
    @Resource
    private IGoodsService goodsService;

    @PostMapping
    public Result save(@RequestBody Cart cart) {
        if (cart.getId() == null) {
            Goods goods = goodsService.getById(cart.getGoodsId());
            if (goods.getInventory()-cart.getNum()<0){
                return Result.error("605","库存不足，无法加购！");
            }
            // 获取当前用户
            Account account = TokenUtils.getCurrentUser();
            // 根据用户 ID 和商品 ID 查询购物车
            Cart one = cartService.getOne(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, account.getId()).eq(Cart::getGoodsId, cart.getGoodsId()));
            // 检查购物车中是否已存在该商品
            if (one != null) {
                // 如果存在，更新商品数量
                int newNum = one.getNum() + cart.getNum();
                one.setNum(newNum);
                cart = one;
            }
            cart.setUserId(account.getId());
        }
        return Result.success(cartService.saveOrUpdate(cart));
    }

    @PostMapping("/num")
    public Result changNum(@RequestBody Cart cart) {
        cartService.updateById(cart);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(cartService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(cartService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId,TokenUtils.getCurrentUser().getId());
        List<Cart> cartList = cartService.list(wrapper);
        // 提取商品 ID
        List<Integer> goodsIds = cartList.stream()
                .map(Cart::getGoodsId)
                .collect(Collectors.toList());
        // 根据商品 ID 获取商品信息
        if (CollectionUtil.isEmpty(goodsIds)) return Result.success(new ArrayList<>());
        List<Goods> goodsList = goodsService.listByIds(goodsIds);

        for (Cart cart : cartList) {
            // 根据商品 ID 查找对应的商品
            Goods goods = goodsList.stream()
                    .filter(g -> g.getId().equals(cart.getGoodsId()))
                    .findFirst()
                    .orElse(null);

            if (goods != null) {
                cart.setGoodsName(goods.getName());
                cart.setGoodsPrice(goods.getPrice());
                cart.setGoodsImg(goods.getImg());
                cart.setGoodsInfo(goods.getInfo());
                cart.setGoodsInventory(goods.getInventory());
            }
        }
        return Result.success(cartList);
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(cartService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Cart::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Cart::getGoodsId, keyword);
        }

        return Result.success(cartService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}

