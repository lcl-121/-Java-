package com.example.springboot.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.BlindBox;
import com.example.springboot.entity.BlindBoxItem;
import com.example.springboot.entity.Goods;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.BlindBoxItemMapper;
import com.example.springboot.mapper.BlindBoxMapper;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.service.IBlindBoxItemService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 盲盒商品项服务实现类
 */
@Service
public class BlindBoxItemServiceImpl extends ServiceImpl<BlindBoxItemMapper, BlindBoxItem> implements IBlindBoxItemService {

    @Resource
    private BlindBoxMapper blindBoxMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public List<BlindBoxItem> selectByBoxId(Integer boxId) {
        LambdaQueryWrapper<BlindBoxItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlindBoxItem::getBoxId, boxId);
        wrapper.orderByAsc(BlindBoxItem::getSort);
        return this.list(wrapper);
    }

    /**
     * 随机抽取商品（根据权重）
     */
    @Transactional(rollbackFor = Exception.class)
    public BlindBoxItem draw(Integer boxId) {
        // 查询盲盒信息
        BlindBox blindBox = blindBoxMapper.selectById(boxId);
        if (blindBox == null) {
            throw new ServiceException("600", "盲盒活动不存在");
        }

        // 检查活动时间
        Date now = new Date();
        if (now.before(blindBox.getStartTime())) {
            throw new ServiceException("600", "活动尚未开始");
        }
        if (now.after(blindBox.getEndTime())) {
            throw new ServiceException("600", "活动已结束");
        }

        // 检查活动状态
        if (blindBox.getStatus() != 1) {
            throw new ServiceException("600", "活动已下架");
        }

        // 检查总次数限制
        if (blindBox.getTotalCount() > 0 && blindBox.getUsedCount() >= blindBox.getTotalCount()) {
            throw new ServiceException("600", "活动次数已用完");
        }

        // 查询所有商品项
        List<BlindBoxItem> items = selectByBoxId(boxId);
        if (items == null || items.isEmpty()) {
            throw new ServiceException("600", "盲盒中没有商品");
        }

        // 计算总权重
        int totalWeight = items.stream()
                .filter(item -> item.getStock() == 0 || item.getUsedStock() < item.getStock())
                .mapToInt(BlindBoxItem::getWeight)
                .sum();

        if (totalWeight == 0) {
            throw new ServiceException("600", "没有可抽取的商品");
        }

        // 根据权重随机选择
        int randomWeight = RandomUtil.randomInt(1, totalWeight + 1);
        int currentWeight = 0;
        BlindBoxItem selectedItem = null;

        for (BlindBoxItem item : items) {
            // 跳过库存已用完的商品
            if (item.getStock() > 0 && item.getUsedStock() >= item.getStock()) {
                continue;
            }

            currentWeight += item.getWeight();
            if (randomWeight <= currentWeight) {
                selectedItem = item;
                break;
            }
        }

        if (selectedItem == null) {
            throw new ServiceException("600", "抽取失败，请重试");
        }

        // 查询商品信息
        Goods goods = goodsMapper.selectById(selectedItem.getGoodsId());
        if (goods == null) {
            throw new ServiceException("600", "商品不存在");
        }

        // 检查商品库存
        if (goods.getInventory() <= 0) {
            throw new ServiceException("600", "商品库存不足");
        }

        // 扣减盲盒商品库存
        if (selectedItem.getStock() > 0) {
            selectedItem.setUsedStock(selectedItem.getUsedStock() + 1);
            this.updateById(selectedItem);
        }

        // 扣减盲盒总次数
        blindBox.setUsedCount(blindBox.getUsedCount() + 1);
        blindBoxMapper.updateById(blindBox);

        // 扣减商品实际库存
        goods.setInventory(goods.getInventory() - 1);
        goodsMapper.updateById(goods);

        // 设置商品信息
        selectedItem.setGoodsName(goods.getName());
        selectedItem.setGoodsImg(goods.getImg());
        selectedItem.setGoodsPrice(goods.getPrice());

        return selectedItem;
    }
}
