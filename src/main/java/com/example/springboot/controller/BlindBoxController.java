package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.BlindBox;
import com.example.springboot.entity.BlindBoxItem;
import com.example.springboot.entity.BlindBoxRecord;
import com.example.springboot.service.IBlindBoxItemService;
import com.example.springboot.service.IBlindBoxRecordService;
import com.example.springboot.service.IBlindBoxService;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.service.IOrdersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 盲盒活动控制器
 */
@RestController
@RequestMapping("/blindbox")
public class BlindBoxController {

    @Resource
    private IBlindBoxService blindBoxService;

    @Resource
    private IBlindBoxItemService blindBoxItemService;

    @Resource
    private IBlindBoxRecordService blindBoxRecordService;

    @Resource
    private IOrdersService ordersService;

    @Resource
    private IGoodsService goodsService;

    /**
     * 保存盲盒活动
     */
    @PostMapping
    public Result save(@RequestBody BlindBox blindBox) {
        return Result.success(blindBoxService.saveOrUpdate(blindBox));
    }

    /**
     * 删除盲盒活动
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(blindBoxService.removeById(id));
    }

    /**
     * 查询单个盲盒活动
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(blindBoxService.getById(id));
    }

    /**
     * 分页查询盲盒活动
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String keyword) {
        LambdaQueryWrapper<BlindBox> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(keyword), BlindBox::getName, keyword);
        wrapper.orderByDesc(BlindBox::getCreateTime);
        
        Page<BlindBox> page = blindBoxService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    /**
     * 查询所有上架的盲盒活动（前端使用）
     */
    @GetMapping("/list")
    public Result findList() {
        LambdaQueryWrapper<BlindBox> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlindBox::getStatus, 1);
        wrapper.orderByDesc(BlindBox::getCreateTime);
        
        List<BlindBox> list = blindBoxService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 获取盲盒商品列表（含概率）
     */
    @GetMapping("/items/{boxId}")
    public Result findItems(@PathVariable Integer boxId) {
        List<BlindBoxItem> items = blindBoxItemService.selectByBoxId(boxId);
        
        // 计算总权重
        int totalWeight = items.stream()
                .filter(item -> item.getStock() == 0 || item.getUsedStock() < item.getStock())
                .mapToInt(BlindBoxItem::getWeight)
                .sum();
        
        // 为每个商品项计算概率
        for (BlindBoxItem item : items) {
            if (totalWeight > 0) {
                double probability = (double) item.getWeight() / totalWeight * 100;
                item.setGoodsName(String.format("%.2f%%", probability));
            }
        }
        
        return Result.success(items);
    }

    /**
     * 保存盲盒商品项
     */
    @PostMapping("/item")
    public Result saveItem(@RequestBody BlindBoxItem item) {
        return Result.success(blindBoxItemService.saveOrUpdate(item));
    }

    /**
     * 删除盲盒商品项
     */
    @DeleteMapping("/item/{id}")
    public Result deleteItem(@PathVariable Integer id) {
        return Result.success(blindBoxItemService.removeById(id));
    }

    /**
     * 批量保存盲盒商品项
     */
    @PostMapping("/items/batch")
    public Result saveItemsBatch(@RequestBody List<BlindBoxItem> items) {
        // 先删除该盲盒下的所有商品项
        if (!items.isEmpty()) {
            Integer boxId = items.get(0).getBoxId();
            LambdaQueryWrapper<BlindBoxItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlindBoxItem::getBoxId, boxId);
            blindBoxItemService.remove(wrapper);
            
            // 再批量保存
            for (BlindBoxItem item : items) {
                blindBoxItemService.save(item);
            }
        }
        return Result.success();
    }

    /**
     * 抽取盲盒（用户参与）
     */
    @PostMapping("/draw/{boxId}")
    public Result draw(@PathVariable Integer boxId) {
        try {
            // 获取当前登录用户
            com.example.springboot.entity.Account currentUser = com.example.springboot.utils.TokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("401", "请先登录");
            }
            
            // 抽取商品
            BlindBoxItem selectedItem = blindBoxItemService.draw(boxId);
            
            // 获取盲盒信息
            BlindBox blindBox = blindBoxService.getById(boxId);
            if (blindBox == null) {
                return Result.error("404", "盲盒活动不存在");
            }
            
            // 保存参与记录
            BlindBoxRecord record = new BlindBoxRecord();
            record.setBoxId(boxId);
            record.setUserId(currentUser.getId());
            record.setGoodsId(selectedItem.getGoodsId());
            record.setPrice(selectedItem.getGoodsPrice());
            record.setStatus(0);
            blindBoxRecordService.save(record);
            
            // 创建订单
            com.example.springboot.entity.Orders order = new com.example.springboot.entity.Orders();
            order.setUserId(currentUser.getId());
            order.setGoodsId(selectedItem.getGoodsId());
            order.setNum(1);
            order.setPrice(selectedItem.getGoodsPrice());
            order.setStatus("0"); // 0=待填写收货信息
            order.setName("盲盒抽取：" + blindBox.getName());
            order.setImg(selectedItem.getGoodsImg());
            
            // 生成订单号（格式：BLIND+ 年月日时分秒 + 随机数）
            String orderNo = "BLIND" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                String.format("%04d", (int)(Math.random() * 10000));
            order.setNo(orderNo);
            
            // 设置下单时间
            order.setTime(java.time.LocalDateTime.now().toString());
            
            // 获取商品信息中的商家 ID
            if (selectedItem.getGoodsId() != null) {
                com.example.springboot.entity.Goods goods = goodsService.getById(selectedItem.getGoodsId());
                if (goods != null && goods.getUnitId() != null) {
                    order.setUnitId(goods.getUnitId());
                }
            }
            
            // 盲盒商品也需要填写收货信息，初始状态为待填写
            order.setUserName(""); // 待用户填写
            order.setUserPhone(""); // 待用户填写
            order.setUserAddress(""); // 待用户填写
            
            // 初始未评价
            order.setRate(0); // 0 表示未评价
            order.setComment(""); // 暂无评价
            order.setReply(""); // 暂无回复
            
            // 保存订单
            ordersService.save(order);
            
            Map<String, Object> result = new HashMap<>();
            result.put("goods", selectedItem);
            result.put("record", record);
            result.put("order", order);
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("600", e.getMessage());
        }
    }

    /**
     * 查询用户参与记录
     */
    @GetMapping("/records")
    public Result findRecords(@RequestParam Integer pageNum,
                              @RequestParam Integer pageSize,
                              @RequestParam(required = false) Integer boxId) {
        Page<BlindBoxRecord> page = blindBoxRecordService.selectPage(
            new Page<>(pageNum, pageSize), boxId);
        return Result.success(page);
    }

    /**
     * 管理员查询参与记录
     */
    @GetMapping("/admin/records")
    public Result findAdminRecords(@RequestParam Integer pageNum,
                                   @RequestParam Integer pageSize,
                                   @RequestParam(required = false) Integer boxId) {
        Page<BlindBoxRecord> page = blindBoxRecordService.selectPage(
            new Page<>(pageNum, pageSize), boxId);
        return Result.success(page);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats/{boxId}")
    public Result getStats(@PathVariable Integer boxId) {
        BlindBox blindBox = blindBoxService.getById(boxId);
        if (blindBox == null) {
            return Result.error("404", "盲盒活动不存在");
        }
        
        // 统计参与人数
        LambdaQueryWrapper<BlindBoxRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlindBoxRecord::getBoxId, boxId);
        wrapper.select(BlindBoxRecord::getUserId).groupBy(BlindBoxRecord::getUserId);
        long userCount = blindBoxRecordService.count(wrapper);
        
        // 统计总收益
        BigDecimal totalRevenue = blindBox.getUsedCount() != null ? 
            blindBox.getPrice().multiply(new BigDecimal(blindBox.getUsedCount())) : BigDecimal.ZERO;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("usedCount", blindBox.getUsedCount());
        stats.put("userCount", userCount);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCount", blindBox.getTotalCount());
        
        return Result.success(stats);
    }

    /**
     * 更新盲盒状态
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        BlindBox blindBox = blindBoxService.getById(id);
        if (blindBox == null) {
            return Result.error("404", "盲盒活动不存在");
        }
        blindBox.setStatus(status);
        blindBoxService.updateById(blindBox);
        return Result.success();
    }
}
