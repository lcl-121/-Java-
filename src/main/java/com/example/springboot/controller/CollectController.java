package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Tag;
import com.example.springboot.service.ICollectService;
import com.example.springboot.service.ITagService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private ICollectService collectService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Collect collect) {

        Integer userId = TokenUtils.getCurrentUser().getId();
        collect.setUserId(userId);

        try{
            collectService.saveOrUpdate(collect);
        }catch (Exception e){
            LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collect::getUserId, userId);
            wrapper.eq(Collect::getItemId,collect.getItemId());
            collectService.remove(wrapper);
            return Result.error("605","取消收藏成功");
        }

        return Result.success();
    }

    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        Integer userId = TokenUtils.getCurrentUser().getId();

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId);
        wrapper.eq(Collect::getItemId,id);
        collectService.remove(wrapper);

        return Result.success();
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(collectService.removeByIds(ids));
    }


    //查询所有
    @GetMapping
    public Result findAll() {
        return Result.success(collectService.list());
    }

    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(collectService.getById(id));
    }

    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Collect::getId);

        return Result.success(collectService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


}

