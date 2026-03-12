package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Address;
import com.example.springboot.entity.Notice;
import com.example.springboot.service.IAddressService;
import com.example.springboot.service.INoticeService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址模块控制器
 * 处理用户收货地址的新增、修改、删除、查询、分页查询等核心操作
 * 接口根路径：/address
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    /**
     * 注入地址业务层服务，封装地址相关数据库操作逻辑
     */
    @Resource
    private IAddressService addressService;

    /**
     * 新增/更新收货地址
     * POST /address
     * @param address 地址实体（JSON请求体），包含姓名、电话、省市区、详细地址、用户ID等字段
     * @return Result 统一响应结果，返回操作成功/失败状态及数据
     * 逻辑：新增时自动绑定当前登录用户ID，更新时保留原用户关联关系
     */
    @PostMapping
    public Result save(@RequestBody Address address) {
        // 新增地址：自动填充当前登录用户ID
        if(address.getId() == null){
            address.setUserId(TokenUtils.getCurrentUser().getId());
        }
        // 调用MyBatis-Plus的saveOrUpdate，根据ID是否存在执行新增/更新
        return Result.success(addressService.saveOrUpdate(address));
    }

    /**
     * 根据ID删除单个地址
     * DELETE /address/{id}
     * @param id 地址ID（路径参数）
     * @return Result 统一响应结果，返回删除操作状态
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(addressService.removeById(id));
    }

    /**
     * 批量删除地址
     * POST /address/del/batch
     * @param ids 地址ID列表（JSON请求体）
     * @return Result 统一响应结果，返回批量删除操作状态
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(addressService.removeByIds(ids));
    }

    /**
     * 查询当前登录用户的所有收货地址
     * GET /address
     * @return Result 统一响应结果，返回当前用户的地址列表
     * 逻辑：通过用户ID过滤，仅返回当前登录用户的地址
     */
    @GetMapping
    public Result findAll() {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        // 条件：地址所属用户ID = 当前登录用户ID
        wrapper.eq(Address::getUserId, TokenUtils.getCurrentUser().getId());
        return Result.success(addressService.list(wrapper));
    }

    /**
     * 根据ID查询单个地址详情
     * GET /address/{id}
     * @param id 地址ID（路径参数）
     * @return Result 统一响应结果，返回指定ID的地址详情
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(addressService.getById(id));
    }

    /**
     * 地址分页查询（支持关键词搜索、权限过滤）
     * GET /address/page
     * @param pageNum 当前页码（必填）
     * @param pageSize 每页条数（必填）
     * @param keyword 搜索关键词（可选），模糊匹配地址姓名
     * @return Result 统一响应结果，返回分页地址列表及分页信息
     * 逻辑：1. 管理员可查所有地址；2. 普通用户仅查自己的地址；3. 按ID降序排列
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        // 默认按地址ID降序，最新地址优先展示
        queryWrapper.orderByDesc(Address::getId);

        // 关键词非空时，模糊匹配地址姓名
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Address::getName, keyword);
        }

        // 获取当前登录用户信息，做权限过滤
        Account account = TokenUtils.getCurrentUser();
        // 非管理员用户：仅查询自己的地址
        if(!StrUtil.equals(account.getRole(),"ROLE_ADMIN")){
            queryWrapper.eq(Address::getUserId, account.getId());
        }

        // 分页查询：Page对象封装页码/条数，queryWrapper封装查询条件
        return Result.success(addressService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
}