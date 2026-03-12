package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.lang.ref.PhantomReference;
import java.math.BigDecimal;
import java.util.BitSet;

/**
 * <p>
 * 实体类
 * </p>
 */

@Data
@TableName(value = "cart")
public class Cart {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;

    private Integer num;

    private Integer userId;

    private Integer unitId;

    @TableField(exist = false)
    private String goodsName;

    @TableField(exist = false)
    private String goodsImg;

    @TableField(exist = false)
    private BigDecimal goodsPrice;

    @TableField(exist = false)
    private String goodsInfo;

    @TableField(exist = false)
    private Integer goodsInventory;
}
