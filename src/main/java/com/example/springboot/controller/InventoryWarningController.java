package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Goods;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.ISysConfigService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 库存预警控制器
 */
@RestController
@RequestMapping("/inventory")
public class InventoryWarningController {

    @Resource
    private ISysConfigService sysConfigService;

    @Resource
    private IGoodsService goodsService;

    /**
     * 获取当前库存预警阈值
     */
    @GetMapping("/threshold")
    public Result getThreshold() {
        Integer threshold = sysConfigService.getInventoryThreshold();
        return Result.success(threshold);
    }

    /**
     * 设置库存预警阈值（仅管理员可访问）
     */
    @PostMapping("/threshold")
    public Result setThreshold(@RequestBody Integer threshold) {
        if (threshold == null || threshold < 0) {
            return Result.error("400", "阈值必须为正整数");
        }
        sysConfigService.setInventoryThreshold(threshold);
        return Result.success();
    }

    /**
     * 获取需要预警的商品列表（商家后台使用）
     */
    @GetMapping("/warning/list")
    public Result getWarningList(@RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize,
                                  @RequestParam(defaultValue = "") String keyword) {
        // 获取当前登录用户
        Account currentUser = TokenUtils.getCurrentUser();

        // 获取库存预警阈值
        Integer threshold = sysConfigService.getInventoryThreshold();

        // 构建查询条件
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询当前商家的商品（非管理员）
        if (!StrUtil.equals(currentUser.getRole(), "ROLE_ADMIN")) {
            queryWrapper.eq(Goods::getUnitId, currentUser.getId());
        }
        
        // 查询库存小于阈值的商品
        queryWrapper.lt(Goods::getInventory, threshold);
        
        // 按库存升序排列（库存最少的在前）
        queryWrapper.orderByAsc(Goods::getInventory);

        // 关键词搜索
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(Goods::getName, keyword);
        }

        // 执行分页查询
        Page<Goods> page = goodsService.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 为每个商品设置预警标识
        for (Goods goods : page.getRecords()) {
            goods.setNeedWarning(goods.getInventory() < threshold);
        }

        return Result.success(page);
    }

    /**
     * 获取库存预警统计信息
     */
    @GetMapping("/warning/count")
    public Result getWarningCount() {
        // 获取当前登录用户
        Account currentUser = TokenUtils.getCurrentUser();

        // 获取库存预警阈值
        Integer threshold = sysConfigService.getInventoryThreshold();

        // 构建查询条件
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询当前商家的商品（非管理员）
        if (!StrUtil.equals(currentUser.getRole(), "ROLE_ADMIN")) {
            queryWrapper.eq(Goods::getUnitId, currentUser.getId());
        }
        
        // 查询库存小于阈值的商品数量
        queryWrapper.lt(Goods::getInventory, threshold);

        long count = goodsService.count(queryWrapper);
        return Result.success(count);
    }
}
