package com.example.springboot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Notice;
import com.example.springboot.entity.Type;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.INoticeService;
import com.example.springboot.service.ITypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分类模块 前端控制器
 * 处理商品分类相关的HTTP请求，包含新增、修改、删除、查询、分页、前台展示等操作
 * </p>
 */
@RestController
@RequestMapping("/type") // 该控制器的基础请求路径，所有接口都以/type开头
public class TypeController {

    // 注入分类业务层服务对象，用于处理分类相关的业务逻辑
    @Resource
    private ITypeService typeService;
    // 注入商品业务层服务对象，用于处理商品相关的业务逻辑（关联查询使用）
    @Resource
    private IGoodsService goodsService;

    /**
     * 新增或更新分类接口
     * @param type 前端传递的分类实体对象，包含分类的相关信息（如名称、id、状态等）
     * @return Result 统一返回结果对象，包含操作是否成功的标识和数据
     *                新增时：type对象无id，执行新增操作；更新时：type对象有id，执行更新操作
     */
    @PostMapping // 接收POST类型的请求，请求路径为/type
    public Result save(@RequestBody Type type) {
        // 调用业务层的新增或更新方法，返回操作结果并封装到Result中返回
        return Result.success(typeService.saveOrUpdate(type));
    }

    /**
     * 根据id删除分类接口
     * @param id 要删除的分类id，通过路径参数传递
     * @return Result 统一返回结果对象，包含删除操作是否成功的标识
     */
    @DeleteMapping("/{id}") // 接收DELETE类型的请求，请求路径为/type/{id}
    public Result delete(@PathVariable Integer id) {
        // 调用业务层根据id删除分类的方法，返回操作结果并封装到Result中返回
        return Result.success(typeService.removeById(id));
    }

    /**
     * 批量删除分类接口
     * @param ids 要删除的分类id集合，通过请求体传递
     * @return Result 统一返回结果对象，包含批量删除操作是否成功的标识
     */
    @PostMapping("/del/batch") // 接收POST类型的请求，请求路径为/type/del/batch
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // 调用业务层批量删除分类的方法，返回操作结果并封装到Result中返回
        return Result.success(typeService.removeByIds(ids));
    }

    /**
     * 查询热门分类（带关联商品）接口
     * 无需登录即可访问，返回启用状态的分类，并携带每个分类下上架的前4个商品
     * @return Result 统一返回结果对象，包含带商品列表的热门分类列表
     */
    @AuthAccess // 自定义注解，标识该接口无需认证即可访问
    @GetMapping("/hot") // 接收GET类型的请求，请求路径为/type/hot
    public Result hot() {
        // 创建LambdaQueryWrapper对象，构建分类查询条件
        LambdaQueryWrapper<Type> wrapper = new LambdaQueryWrapper<>();
        // 添加查询条件：只查询状态为启用的分类
        wrapper.eq(Type::getStatus,true);
        // 执行查询，获取启用状态的分类列表
        List<Type> list = typeService.list(wrapper);

        // 遍历每个分类，查询该分类下的关联商品
        for(Type type : list) {
            // 构建商品查询条件
            LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
            // 商品状态为上架
            queryWrapper.eq(Goods::getStatus,true);
            // 商品所属分类id匹配当前分类id
            queryWrapper.eq(Goods::getTypeId,type.getId());
            // 按商品id升序排列
            queryWrapper.orderByAsc(Goods::getId);
            // 只查询前4条数据
            queryWrapper.last("limit 4");
            // 执行查询，获取该分类下的商品列表
            List<Goods> goodsList = goodsService.list(queryWrapper);

            // 商品列表非空则赋值，否则赋值空集合
            if(CollectionUtil.isNotEmpty(goodsList)) {
                type.setGoodsList(goodsList);
            }else {
                type.setGoodsList(new ArrayList<>());
            }
        }
        // 返回带商品列表的分类数据
        return Result.success(list);
    }

    /**
     * 前台查询分类接口
     * 无需登录即可访问，返回前6个分类数据
     * @return Result 统一返回结果对象，包含前6个分类的列表数据
     */
    @AuthAccess // 自定义注解，标识该接口无需认证即可访问
    @GetMapping("/front") // 接收GET类型的请求，请求路径为/type/front
    public Result findAllFront() {
        // 创建LambdaQueryWrapper对象，构建分类查询条件
        LambdaQueryWrapper<Type> wrapper = new LambdaQueryWrapper<>();
        // 只查询前6条分类数据
        wrapper.last("limit 6");
        // 执行查询，获取分类列表
        List<Type> list = typeService.list(wrapper);
        // 返回查询结果
        return Result.success(list);
    }

    /**
     * 查询所有分类接口
     * 无需登录即可访问，返回全部分类数据
     * @return Result 统一返回结果对象，包含所有分类的列表数据
     */
    @AuthAccess // 自定义注解，标识该接口无需认证即可访问
    @GetMapping // 接收GET类型的请求，请求路径为/type
    public Result findAll() {
        // 调用业务层查询所有分类的方法，返回结果并封装到Result中返回
        return Result.success(typeService.list());
    }

    /**
     * 根据id查询单个分类接口
     * @param id 要查询的分类id，通过路径参数传递
     * @return Result 统一返回结果对象，包含查询到的分类实体对象
     */
    @GetMapping("/{id}") // 接收GET类型的请求，请求路径为/type/{id}
    public Result findOne(@PathVariable Integer id) {
        // 调用业务层根据id查询分类的方法，返回结果并封装到Result中返回
        return Result.success(typeService.getById(id));
    }

    /**
     * 分类分页查询接口
     * @param pageNum 当前页码，从前端请求参数中获取
     * @param pageSize 每页显示的记录数，从前端请求参数中获取
     * @param keyword 搜索关键词（分类名称），默认值为空字符串，用于模糊查询
     * @return Result 统一返回结果对象，包含分页查询后的分类列表及分页信息
     */
    @GetMapping("/page") // 接收GET类型的请求，请求路径为/type/page
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {
        // 创建LambdaQueryWrapper对象，用于构建动态查询条件（类型安全，避免硬编码字段名）
        LambdaQueryWrapper<Type> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件：根据分类id降序排列
        queryWrapper.orderByDesc(Type::getId);

        // 如果搜索关键词不为空且不为空白字符串，添加模糊查询条件（分类名称包含关键词）
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Type::getName, keyword);
        }

        // 调用业务层分页查询方法，传入分页对象和查询条件，返回分页结果并封装到Result中返回
        return Result.success(typeService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
}