package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统配置实体类 - 用于存储库存预警阈值等系统参数
 */
@Data
@TableName(value = "sys_config")
public class SysConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

}
