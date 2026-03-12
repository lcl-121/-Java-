package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Notice;
import com.example.springboot.entity.User;
import com.example.springboot.service.INoticeService;
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
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private INoticeService noticeService;

    //新增或更新
    @PostMapping
    public Result save(@RequestBody Notice notice) {
        return Result.success(noticeService.saveOrUpdate(notice));
    }

    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(noticeService.removeById(id));
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(noticeService.removeByIds(ids));
    }

    //查询所有
    @AuthAccess
    @GetMapping
    public Result findAll() {
        return Result.success(noticeService.list());
    }

    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(noticeService.getById(id));
    }

    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Notice::getId);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Notice::getName, keyword);
        }

        return Result.success(noticeService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


}

