package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.BlindBoxItem;

import java.util.List;

/**
 * 盲盒商品项服务接口
 */
public interface IBlindBoxItemService extends IService<BlindBoxItem> {
    
    /**
     * 根据盲盒 ID 查询商品列表
     */
    List<BlindBoxItem> selectByBoxId(Integer boxId);
    
    /**
     * 随机抽取商品（根据权重）
     * @param boxId 盲盒 ID
     * @return 抽中的商品项
     */
    BlindBoxItem draw(Integer boxId);
}
