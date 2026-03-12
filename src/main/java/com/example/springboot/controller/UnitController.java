package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Unit;
import com.example.springboot.entity.User;
import com.example.springboot.service.IUnitService;
import com.example.springboot.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/unit")
public class UnitController {

    @Resource
    private IUnitService unitService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Unit unit) {
        return Result.success(unitService.saveOrUpdate(unit));
    }

    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(unitService.removeById(id));
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(unitService.removeByIds(ids));
    }

    //查询所有
    @GetMapping
    public Result findAll() {
        return Result.success(unitService.list());
    }

    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(unitService.getById(id));
    }

    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Unit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Unit::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Unit::getNickname, keyword);
        }

        return Result.success(unitService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


}

