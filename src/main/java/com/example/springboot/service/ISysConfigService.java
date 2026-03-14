package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.SysConfig;

/**
 * 系统配置服务接口
 */
public interface ISysConfigService extends IService<SysConfig> {

    /**
     * 获取库存预警阈值
     * @return 库存阈值，默认值 10
     */
    Integer getInventoryThreshold();

    /**
     * 设置库存预警阈值
     * @param threshold 阈值
     */
    void setInventoryThreshold(Integer threshold);

}
