package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Admin;
import com.example.springboot.service.IAdminService;
import com.example.springboot.service.ISysConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 管理员模块 前端控制器
 * 处理管理员相关的HTTP请求，提供增删改查、分页查询等接口
 * </p>
 */
@RestController
@RequestMapping("/admin") // 该控制器的基础请求路径
public class AdminController {

    // 注入管理员业务层服务对象
    @Resource
    private IAdminService adminService;

    // 注入系统配置服务
    @Resource
    private ISysConfigService sysConfigService;

    /**
     * 新增/修改管理员信息
     * 新增时admin对象无id，修改时携带id
     * @param admin 管理员实体对象（接收前端JSON参数）
     * @return 统一返回结果Result，包含操作是否成功的布尔值
     */
    @PostMapping
    public Result save(@RequestBody Admin admin) {
        return Result.success(adminService.saveOrUpdate(admin));
    }

    /**
     * 根据ID删除单个管理员
     * @param id 管理员主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含删除是否成功的布尔值
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(adminService.removeById(id));
    }

    /**
     * 批量删除管理员
     * @param ids 管理员主键ID集合（接收前端传递的ID列表）
     * @return 统一返回结果Result，包含批量删除是否成功的布尔值
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(adminService.removeByIds(ids));
    }

    /**
     * 查询所有管理员信息
     * @return 统一返回结果Result，包含所有管理员的列表数据
     */
    @GetMapping
    public Result findAll() {
        return Result.success(adminService.list());
    }

    /**
     * 根据ID查询单个管理员详情
     * @param id 管理员主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含该ID对应的管理员实体对象
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(adminService.getById(id));
    }

    /**
     * 管理员信息分页查询（支持关键词模糊搜索）
     * @param pageNum 当前页码（前端传递，必填）
     * @param pageSize 每页显示条数（前端传递，必填）
     * @param keyword 搜索关键词（默认空字符串，根据昵称模糊查询）
     * @return 统一返回结果Result，包含分页查询后的管理员列表及分页信息
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 构建MyBatis-Plus的Lambda查询条件构造器
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：按管理员ID降序排列
        queryWrapper.orderByDesc(Admin::getId);

        // 如果关键词非空，则添加昵称的模糊查询条件
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Admin::getNickname, keyword);
        }

        // 执行分页查询并返回结果
        return Result.success(adminService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 设置库存预警阈值（仅管理员可访问）
     * @param threshold 阈值
     * @return 统一返回结果
     */
    @PostMapping("/set/threshold")
    public Result setThreshold(@RequestBody Integer threshold) {
        if (threshold == null || threshold < 0) {
            return Result.error("400", "阈值必须为正整数");
        }
        sysConfigService.setInventoryThreshold(threshold);
        return Result.success();
    }

}