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
 * 收藏模块 前端控制器
 * 处理收藏（Collect）相关的HTTP请求，提供收藏/取消收藏、删除、查询、分页等接口
 * </p>
 */
@RestController
@RequestMapping("/collect") // 该控制器的基础请求路径，所有接口均以/collect开头
public class CollectController {

    // 注入收藏业务层服务对象，用于调用收藏相关的业务逻辑
    @Resource
    private ICollectService collectService;

    /**
     * 新增或取消收藏（逻辑：新增失败则判定为已收藏，执行取消收藏操作）
     * 自动关联当前登录用户ID，无需前端传递
     * @param collect 收藏实体对象（接收前端传递的JSON格式参数，至少包含itemId）
     * @return 统一返回结果Result：新增成功返回成功标识，取消收藏返回605错误码+提示
     */
    //新增或更新
    @PostMapping
    public Result save(@RequestBody Collect collect) {
        // 获取当前登录用户的ID
        Integer userId = TokenUtils.getCurrentUser().getId();
        // 将当前用户ID关联到收藏对象
        collect.setUserId(userId);

        try{
            // 尝试执行新增或更新收藏操作
            collectService.saveOrUpdate(collect);
        }catch (Exception e){
            // 新增失败（说明已收藏），构建查询条件查询该用户对该商品的收藏记录
            LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collect::getUserId, userId); // 匹配当前用户ID
            wrapper.eq(Collect::getItemId,collect.getItemId()); // 匹配收藏的商品ID
            // 删除该收藏记录（取消收藏）
            collectService.remove(wrapper);
            // 返回取消收藏成功的结果，自定义605错误码
            return Result.error("605","取消收藏成功");
        }
        // 新增收藏成功，返回成功结果
        return Result.success();
    }

    /**
     * 根据商品ID删除单个收藏记录（仅删除当前登录用户的收藏）
     * @param id 被收藏商品的itemId（从URL路径中获取）
     * @return 统一返回结果Result，包含删除是否成功的布尔值
     */
    //删除接口
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        // 获取当前登录用户的ID
        Integer userId = TokenUtils.getCurrentUser().getId();
        // 构建查询条件：匹配当前用户ID和商品ID
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId);
        wrapper.eq(Collect::getItemId,id);
        // 执行删除操作
        collectService.remove(wrapper);

        return Result.success();
    }

    /**
     * 批量删除收藏记录
     * @param ids 收藏记录的主键ID集合（接收前端传递的ID列表）
     * @return 统一返回结果Result，包含批量删除是否成功的布尔值
     */
    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(collectService.removeByIds(ids));
    }

    /**
     * 查询所有收藏记录（无权限过滤，返回全量数据）
     * @return 统一返回结果Result，包含所有收藏记录的列表数据
     */
    //查询所有
    @GetMapping
    public Result findAll() {
        return Result.success(collectService.list());
    }

    /**
     * 根据收藏记录主键ID查询单个收藏详情
     * @param id 收藏记录的主键ID（从URL路径中获取）
     * @return 统一返回结果Result，包含该ID对应的收藏实体对象
     */
    //查询某一个
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(collectService.getById(id));
    }

    /**
     * 收藏记录分页查询（暂未支持关键词搜索，仅按ID降序排列）
     * @param pageNum 当前页码（前端传递，必填）
     * @param pageSize 每页显示条数（前端传递，必填）
     * @param keyword 搜索关键词（默认空字符串，当前未实际使用）
     * @return 统一返回结果Result，包含分页查询后的收藏列表及分页信息
     */
    //分页接口
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 构建MyBatis-Plus的Lambda查询条件构造器
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：按收藏记录主键ID降序排列
        queryWrapper.orderByDesc(Collect::getId);

        // 执行分页查询并返回结果
        return Result.success(collectService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

}