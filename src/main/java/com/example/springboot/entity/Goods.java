package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 实体类
 * </p>
 */

@Data
@TableName(value = "goods")
public class Goods {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer typeId;

    private String info;

    private String content;

    private String img;

    private String imgList;

    private Integer inventory;

    private BigDecimal price;

    private String unit;

    private Integer unitId;

    private String date;

    private Boolean status;

    private Integer sales;

    @TableField(exist = false)
    private Boolean isCollected;
}
