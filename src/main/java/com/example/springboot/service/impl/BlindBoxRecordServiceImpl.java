package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.BlindBoxRecord;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.BlindBoxRecordMapper;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IBlindBoxRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 盲盒参与记录服务实现类
 */
@Service
public class BlindBoxRecordServiceImpl extends ServiceImpl<BlindBoxRecordMapper, BlindBoxRecord> implements IBlindBoxRecordService {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Page<BlindBoxRecord> selectPage(Page<BlindBoxRecord> page, Integer boxId) {
        LambdaQueryWrapper<BlindBoxRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(boxId != null, BlindBoxRecord::getBoxId, boxId);
        wrapper.orderByDesc(BlindBoxRecord::getCreateTime);
        
        Page<BlindBoxRecord> result = this.page(page, wrapper);
        
        // 填充关联信息
        List<BlindBoxRecord> records = result.getRecords();
        for (BlindBoxRecord record : records) {
            // 查询商品信息
            Goods goods = goodsMapper.selectById(record.getGoodsId());
            if (goods != null) {
                record.setGoodsName(goods.getName());
                record.setGoodsImg(goods.getImg());
            }
            
            // 查询用户信息
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                record.setUsername(user.getNickname());
            }
        }
        
        return result;
    }
}
