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
 * 单位模块 前端控制器
 * 处理单位相关的HTTP请求，包括新增、修改、删除、查询、分页等操作
 * </p>
 */
@RestController
@RequestMapping("/unit") // 该控制器的基础请求路径，所有接口都以/unit开头
public class UnitController {

    // 注入单位业务层服务对象，用于处理单位相关的业务逻辑
    @Resource
    private IUnitService unitService;

    /**
     * 新增或更新单位接口
     * @param unit 前端传递的单位实体对象，包含单位的相关信息（如名称、昵称、id等）
     * @return Result 统一返回结果对象，包含操作是否成功的标识和数据
     *                新增时：unit对象无id，执行新增操作；更新时：unit对象有id，执行更新操作
     */
    //新增或更新
    @PostMapping // 接收POST类型的请求，请求路径为/unit
    public Result save(@RequestBody Unit unit) {
        // 调用业务层的新增或更新方法，返回操作结果并封装到Result中返回
        return Result.success(unitService.saveOrUpdate(unit));
    }

    /**
     * 根据id删除单位接口
     * @param id 要删除的单位id，通过路径参数传递
     * @return Result 统一返回结果对象，包含删除操作是否成功的标识
     */
    //删除接口
    @DeleteMapping("/{id}") // 接收DELETE类型的请求，请求路径为/unit/{id}
    public Result delete(@PathVariable Integer id) {
        // 调用业务层根据id删除单位的方法，返回操作结果并封装到Result中返回
        return Result.success(unitService.removeById(id));
    }

    /**
     * 批量删除单位接口
     * @param ids 要删除的单位id集合，通过请求体传递
     * @return Result 统一返回结果对象，包含批量删除操作是否成功的标识
     */
    //批量删除
    @PostMapping("/del/batch") // 接收POST类型的请求，请求路径为/unit/del/batch
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // 调用业务层批量删除单位的方法，返回操作结果并封装到Result中返回
        return Result.success(unitService.removeByIds(ids));
    }

    /**
     * 查询所有单位接口
     * @return Result 统一返回结果对象，包含所有单位的列表数据
     */
    //查询所有
    @GetMapping // 接收GET类型的请求，请求路径为/unit
    public Result findAll() {
        // 调用业务层查询所有单位的方法，返回结果并封装到Result中返回
        return Result.success(unitService.list());
    }

    /**
     * 根据id查询单个单位接口
     * @param id 要查询的单位id，通过路径参数传递
     * @return Result 统一返回结果对象，包含查询到的单位实体对象
     */
    //查询某一个
    @GetMapping("/{id}") // 接收GET类型的请求，请求路径为/unit/{id}
    public Result findOne(@PathVariable Integer id) {
        // 调用业务层根据id查询单位的方法，返回结果并封装到Result中返回
        return Result.success(unitService.getById(id));
    }

    /**
     * 单位分页查询接口
     * @param pageNum 当前页码，从前端请求参数中获取
     * @param pageSize 每页显示的记录数，从前端请求参数中获取
     * @param keyword 搜索关键词（单位昵称），默认值为空字符串，用于模糊查询
     * @return Result 统一返回结果对象，包含分页查询后的单位列表及分页信息
     */
    //分页接口
    @GetMapping("/page") // 接收GET类型的请求，请求路径为/unit/page
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 创建LambdaQueryWrapper对象，用于构建动态查询条件（类型安全，避免硬编码字段名）
        LambdaQueryWrapper<Unit> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：根据单位id降序排列
        queryWrapper.orderByDesc(Unit::getId);

        // 如果搜索关键词不为空且不为空白字符串，添加模糊查询条件（单位昵称包含关键词）
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Unit::getNickname, keyword);
        }

        // 调用业务层分页查询方法，传入分页对象和查询条件，返回分页结果并封装到Result中返回
        return Result.success(unitService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
}