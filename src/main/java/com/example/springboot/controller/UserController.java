package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户模块 前端控制器
 * 处理用户相关的HTTP请求，包含新增、修改、删除、查询、分页等核心操作
 * </p>
 */
@RestController
@RequestMapping("/user") // 该控制器的基础请求路径，所有用户相关接口都以/user开头
public class UserController {

    // 注入用户业务层服务对象，用于处理用户相关的业务逻辑
    @Resource
    private IUserService userService;

    /**
     * 新增或更新用户接口
     * @param user 前端传递的用户实体对象，包含用户的昵称、账号、密码等相关信息
     * @return Result 统一返回结果对象，包含操作是否成功的标识和数据
     *                新增时：user对象无id，执行新增操作；更新时：user对象有id，执行更新操作
     */
    //新增或更新
    @PostMapping // 接收POST类型的请求，请求路径为/user
    public Result save(@RequestBody User user) {
        // 调用业务层的新增或更新方法，返回操作结果并封装到Result中返回
        return Result.success(userService.saveOrUpdate(user));
    }

    /**
     * 根据id删除用户接口
     * @param id 要删除的用户id，通过路径参数传递
     * @return Result 统一返回结果对象，包含删除操作是否成功的标识
     */
    //删除接口
    @DeleteMapping("/{id}") // 接收DELETE类型的请求，请求路径为/user/{id}
    public Result delete(@PathVariable Integer id) {
        // 调用业务层根据id删除用户的方法，返回操作结果并封装到Result中返回
        return Result.success(userService.removeById(id));
    }

    /**
     * 批量删除用户接口
     * @param ids 要删除的用户id集合，通过请求体传递
     * @return Result 统一返回结果对象，包含批量删除操作是否成功的标识
     */
    //批量删除
    @PostMapping("/del/batch") // 接收POST类型的请求，请求路径为/user/del/batch
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // 调用业务层批量删除用户的方法，返回操作结果并封装到Result中返回
        return Result.success(userService.removeByIds(ids));
    }

    /**
     * 查询所有用户接口
     * @return Result 统一返回结果对象，包含所有用户的列表数据
     */
    //查询所有
    @GetMapping // 接收GET类型的请求，请求路径为/user
    public Result findAll() {
        // 调用业务层查询所有用户的方法，返回结果并封装到Result中返回
        return Result.success(userService.list());
    }

    /**
     * 根据id查询单个用户接口
     * @param id 要查询的用户id，通过路径参数传递
     * @return Result 统一返回结果对象，包含查询到的用户实体对象
     */
    //查询某一个
    @GetMapping("/{id}") // 接收GET类型的请求，请求路径为/user/{id}
    public Result findOne(@PathVariable Integer id) {
        // 调用业务层根据id查询用户的方法，返回结果并封装到Result中返回
        return Result.success(userService.getById(id));
    }

    /**
     * 用户分页查询接口
     * @param pageNum 当前页码，从前端请求参数中获取
     * @param pageSize 每页显示的记录数，从前端请求参数中获取
     * @param keyword 搜索关键词（用户昵称），默认值为空字符串，用于模糊查询
     * @return Result 统一返回结果对象，包含分页查询后的用户列表及分页信息
     */
    //分页接口
    @GetMapping("/page") // 接收GET类型的请求，请求路径为/user/page
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {

        // 创建LambdaQueryWrapper对象，用于构建动态查询条件（类型安全，避免硬编码字段名）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：根据用户id降序排列
        queryWrapper.orderByDesc(User::getId);

        // 如果搜索关键词不为空且不为空白字符串，添加模糊查询条件（用户昵称包含关键词）
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(User::getNickname, keyword);
        }

        // 调用业务层分页查询方法，传入分页对象和查询条件，返回分页结果并封装到Result中返回
        return Result.success(userService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
}