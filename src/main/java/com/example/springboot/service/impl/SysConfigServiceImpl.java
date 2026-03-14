package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.SysConfig;
import com.example.springboot.mapper.SysConfigMapper;
import com.example.springboot.service.ISysConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现类
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    @Resource
    private SysConfigMapper sysConfigMapper;

    private static final String INVENTORY_THRESHOLD_KEY = "inventory.warning.threshold";
    private static final String DEFAULT_THRESHOLD = "10";

    @Override
    public Integer getInventoryThreshold() {
        // 查询数据库中的配置
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, INVENTORY_THRESHOLD_KEY);
        SysConfig sysConfig = sysConfigMapper.selectOne(wrapper);

        if (sysConfig != null && StrUtil.isNotBlank(sysConfig.getConfigValue())) {
            try {
                return Integer.parseInt(sysConfig.getConfigValue());
            } catch (NumberFormatException e) {
                return Integer.parseInt(DEFAULT_THRESHOLD);
            }
        }

        // 如果没有配置，返回默认值
        return Integer.parseInt(DEFAULT_THRESHOLD);
    }

    @Override
    public void setInventoryThreshold(Integer threshold) {
        // 查询是否已存在配置
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, INVENTORY_THRESHOLD_KEY);
        SysConfig sysConfig = sysConfigMapper.selectOne(wrapper);

        if (sysConfig != null) {
            // 更新现有配置
            sysConfig.setConfigValue(threshold.toString());
            sysConfig.setDescription("商品库存预警阈值，低于此值将显示预警提示");
            sysConfigMapper.updateById(sysConfig);
        } else {
            // 插入新配置
            SysConfig newConfig = new SysConfig();
            newConfig.setConfigKey(INVENTORY_THRESHOLD_KEY);
            newConfig.setConfigValue(threshold.toString());
            newConfig.setDescription("商品库存预警阈值，低于此值将显示预警提示");
            sysConfigMapper.insert(newConfig);
        }
    }
}
