package com.example.springboot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Notice;
import com.example.springboot.entity.Type;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.INoticeService;
import com.example.springboot.service.ITypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/type")
public class TypeController {

    @Resource
    private ITypeService typeService;
    @Resource
    private IGoodsService goodsService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Type type) {
        return Result.success(typeService.saveOrUpdate(type));
    }

    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(typeService.removeById(id));
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(typeService.removeByIds(ids));
    }

    @AuthAccess
    @GetMapping("/hot")
    public Result hot() {
        LambdaQueryWrapper<Type> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Type::getStatus,true);
        List<Type> list = typeService.list(wrapper);

        for(Type type : list) {
            //遍历所有的分类，并且找到这个分类下所有上架的商品，然后给他们拿出来
            LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Goods::getStatus,true);
            queryWrapper.eq(Goods::getTypeId,type.getId());
            queryWrapper.orderByAsc(Goods::getId);
            queryWrapper.last("limit 4");
            List<Goods> goodsList = goodsService.list(queryWrapper);
            if(CollectionUtil.isNotEmpty(goodsList)) {
                type.setGoodsList(goodsList);
            }else {
                type.setGoodsList(new ArrayList<>());
            }
        }

        return Result.success(list);
    }

    @AuthAccess
    @GetMapping("/front")
    public Result findAllFront() {

        LambdaQueryWrapper<Type> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("limit 6");
        List<Type> list = typeService.list(wrapper);
        return Result.success(list);
    }

    //查询所有
    @AuthAccess
    @GetMapping
    public Result findAll() {
        return Result.success(typeService.list());
    }

    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(typeService.getById(id));
    }

    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Type> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Type::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Type::getName, keyword);
        }

        return Result.success(typeService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


}

