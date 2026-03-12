package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Banner;
import com.example.springboot.entity.Type;
import com.example.springboot.service.IBannerService;
import com.example.springboot.service.ITypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 轮播图模块 前端控制器
 * 处理轮播图（Banner）相关的HTTP请求，提供增删改查、分页查询等接口
 * </p>
 */
@RestController
@RequestMapping("/banner") // 该控制器的基础请求路径，所有接口均以/banner开头
public class BannerController {

    // 注入轮播图业务层服务对象，用于调用轮播图相关的业务逻辑
    @Resource
    private IBannerService bannerService;

    /**
     * 新增或更新轮播图信息
     * 新增时banner对象无id，更新时banner对象携带已有id
     * @param banner 轮播图实体对象（接收前端传递的JSON格式参数）
     * @return 统一返回结果Result，包含操作是否成功的布尔值
     */
    @PostMapping
    public Result save(@RequestBody Banner banner) {
        return Result.success(bannerService.saveOrUpdate(banner));
    }

    /**
     * 根据ID删除单个轮播图
     * @param id 轮播图主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含删除是否成功的布尔值
     */
    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(bannerService.removeById(id));
    }

    /**
     * 批量删除轮播图
     * @param ids 轮播图主键ID集合（接收前端传递的ID列表）
     * @return 统一返回结果Result，包含批量删除是否成功的布尔值
     */
    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(bannerService.removeByIds(ids));
    }

    /**
     * 查询所有轮播图信息（无需登录即可访问）
     * @AuthAccess 自定义注解，标识该接口跳过登录验证
     * @return 统一返回结果Result，包含所有轮播图的列表数据
     */
    //查询所有
    @AuthAccess
    @GetMapping
    public Result findAll() {
        return Result.success(bannerService.list());
    }

    /**
     * 根据ID查询单个轮播图详情
     * @param id 轮播图主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含该ID对应的轮播图实体对象
     */
    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(bannerService.getById(id));
    }

    /**
     * 轮播图信息分页查询（支持名称关键词模糊搜索）
     * @param pageNum 当前页码（前端传递，必填）
     * @param pageSize 每页显示条数（前端传递，必填）
     * @param keyword 搜索关键词（默认空字符串，根据轮播图名称模糊查询）
     * @return 统一返回结果Result，包含分页查询后的轮播图列表及分页信息
     */
    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 构建MyBatis-Plus的Lambda查询条件构造器，用于拼接查询条件
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：按轮播图ID降序排列
        queryWrapper.orderByDesc(Banner::getId);

        // 如果关键词非空，则添加轮播图名称的模糊查询条件
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Banner::getName, keyword);
        }

        // 执行分页查询：构建分页对象并传入查询条件，返回分页结果
        return Result.success(bannerService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}