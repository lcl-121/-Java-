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
 * 通知公告模块 前端控制器
 * 处理通知公告（Notice）相关的HTTP请求，提供增删改查、分页查询等接口
 * </p>
 */
@RestController
@RequestMapping("/notice") // 该控制器的基础请求路径，所有接口均以/notice开头
public class NoticeController {

    // 注入通知公告业务层服务对象，用于调用通知相关的业务逻辑
    @Resource
    private INoticeService noticeService;

    /**
     * 新增/更新通知公告
     * 若通知ID为空则执行新增，ID不为空则执行更新
     * @param notice 通知公告实体对象（接收前端传递的JSON格式参数）
     * @return 统一返回结果Result，包含新增/更新是否成功的布尔值
     */
    @PostMapping
    public Result save(@RequestBody Notice notice) {
        return Result.success(noticeService.saveOrUpdate(notice));
    }

    /**
     * 根据ID删除单个通知公告
     * @param id 通知公告主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含删除是否成功的布尔值
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(noticeService.removeById(id));
    }

    /**
     * 批量删除通知公告
     * @param ids 通知公告主键ID集合（接收前端传递的ID列表）
     * @return 统一返回结果Result，包含批量删除是否成功的布尔值
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(noticeService.removeByIds(ids));
    }

    /**
     * 查询所有通知公告（无需登录即可访问）
     * @return 统一返回结果Result，包含所有通知公告的列表
     */
    @AuthAccess // 自定义注解：标识该接口无需登录即可访问
    @GetMapping
    public Result findAll() {
        return Result.success(noticeService.list());
    }

    /**
     * 根据ID查询单个通知公告详情
     * @param id 通知公告主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含单个通知公告的实体对象
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(noticeService.getById(id));
    }

    /**
     * 通知公告分页查询（支持关键词模糊搜索）
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页显示条数（必填）
     * @param keyword 搜索关键词（默认空字符串：不搜索，匹配通知名称）
     * @return 统一返回结果Result，包含分页后的通知列表及分页信息
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 构建通知公告查询条件构造器
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        // 默认按通知ID降序排序（最新创建的通知排在前面）
        queryWrapper.orderByDesc(Notice::getId);

        // 关键词搜索：关键词非空时，按通知名称模糊查询
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Notice::getName, keyword);
        }

        // 执行分页查询并返回结果
        return Result.success(noticeService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}