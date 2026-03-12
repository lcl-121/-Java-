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
 * 标签模块 前端控制器
 * 处理标签相关的HTTP请求，包括新增、修改、删除、查询等操作
 * </p>
 */
@RestController
@RequestMapping("/tag") // 该控制器的基础请求路径，所有接口都以/tag开头
public class TagController {

    // 注入标签业务层服务对象，用于处理标签相关的业务逻辑
    @Resource
    private ITagService tagService;

    /**
     * 新增或更新标签接口
     * @param tag 前端传递的标签实体对象，包含标签的相关信息（如名称、id等）
     * @return Result 统一返回结果对象，包含操作是否成功的标识和数据
     *                新增时：tag对象无id，执行新增操作；更新时：tag对象有id，执行更新操作
     */
    @PostMapping // 接收POST类型的请求，请求路径为/tag
    public Result save(@RequestBody Tag tag) {
        // 调用业务层的新增或更新方法，返回操作结果并封装到Result中返回
        return Result.success(tagService.saveOrUpdate(tag));
    }

    /**
     * 根据id删除标签接口
     * @param id 要删除的标签id，通过路径参数传递
     * @return Result 统一返回结果对象，包含删除操作是否成功的标识
     */
    @DeleteMapping("/{id}") // 接收DELETE类型的请求，请求路径为/tag/{id}
    public Result delete(@PathVariable Integer id) {
        // 调用业务层根据id删除标签的方法，返回操作结果并封装到Result中返回
        return Result.success(tagService.removeById(id));
    }

    /**
     * 批量删除标签接口
     * @param ids 要删除的标签id集合，通过请求体传递
     * @return Result 统一返回结果对象，包含批量删除操作是否成功的标识
     */
    @PostMapping("/del/batch") // 接收POST类型的请求，请求路径为/tag/del/batch
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // 调用业务层批量删除标签的方法，返回操作结果并封装到Result中返回
        return Result.success(tagService.removeByIds(ids));
    }

    /**
     * 查询所有标签接口
     * @return Result 统一返回结果对象，包含所有标签的列表数据
     */
    @GetMapping // 接收GET类型的请求，请求路径为/tag
    public Result findAll() {
        // 调用业务层查询所有标签的方法，返回结果并封装到Result中返回
        return Result.success(tagService.list());
    }

    /**
     * 根据id查询单个标签接口
     * @param id 要查询的标签id，通过路径参数传递
     * @return Result 统一返回结果对象，包含查询到的标签实体对象
     */
    @GetMapping("/{id}") // 接收GET类型的请求，请求路径为/tag/{id}
    public Result findOne(@PathVariable Integer id) {
        // 调用业务层根据id查询标签的方法，返回结果并封装到Result中返回
        return Result.success(tagService.getById(id));
    }

    /**
     * 标签分页查询接口
     * @param pageNum 当前页码，从前端请求参数中获取
     * @param pageSize 每页显示的记录数，从前端请求参数中获取
     * @param keyword 搜索关键词（标签名称），默认值为空字符串，用于模糊查询
     * @return Result 统一返回结果对象，包含分页查询后的标签列表及分页信息
     */
    @GetMapping("/page") // 接收GET类型的请求，请求路径为/tag/page
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 创建LambdaQueryWrapper对象，用于构建动态查询条件（类型安全，避免硬编码字段名）
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：根据标签id降序排列
        queryWrapper.orderByDesc(Tag::getId);

        // 如果搜索关键词不为空且不为空白字符串，添加模糊查询条件（标签名称包含关键词）
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Tag::getName, keyword);
        }

        // 调用业务层分页查询方法，传入分页对象和查询条件，返回分页结果并封装到Result中返回
        return Result.success(tagService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
}