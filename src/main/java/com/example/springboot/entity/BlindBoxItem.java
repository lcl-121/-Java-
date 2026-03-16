package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盲盒商品项实体类
 */
@Data
@TableName("blind_box_item")
public class BlindBoxItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer boxId;

    private Integer goodsId;

    private Integer weight;

    private Integer stock;

    private Integer usedStock;

    private Integer sort;

    private Date createTime;

    private Date updateTime;

    // 关联商品信息（非数据库字段）
    @TableField(exist = false)
    private String goodsName;
    
    @TableField(exist = false)
    private String goodsImg;
    
    @TableField(exist = false)
    private BigDecimal goodsPrice;
}
