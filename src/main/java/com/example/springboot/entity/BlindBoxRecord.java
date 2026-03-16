package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盲盒参与记录实体类
 */
@Data
@TableName("blind_box_record")
public class BlindBoxRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer boxId;

    private Integer userId;

    private Integer goodsId;

    private BigDecimal price;

    private Integer status;

    private Date createTime;

    // 关联商品信息（非数据库字段）
    @TableField(exist = false)
    private String goodsName;
    
    @TableField(exist = false)
    private String goodsImg;
    
    // 用户名（非数据库字段）
    @TableField(exist = false)
    private String username;
}
