package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Tag;
import com.example.springboot.entity.Type;
import com.example.springboot.service.ITagService;
import com.example.springboot.service.ITypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private ITagService tagService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Tag tag) {
        return Result.success(tagService.saveOrUpdate(tag));
    }

    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(tagService.removeById(id));
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(tagService.removeByIds(ids));
    }


    //查询所有
    @GetMapping
    public Result findAll() {
        return Result.success(tagService.list());
    }

    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(tagService.getById(id));
    }

    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Tag::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Tag::getName, keyword);
        }

        return Result.success(tagService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


}

