package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 实体类
 * </p>
 */

@Data
@TableName(value = "orders")
public class Orders {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String no;

    private String time;

    private String name;

    private String img;

    private Integer goodsId;

    private Integer userId;

    private Integer unitId;

    private Integer num;

    private BigDecimal price;

    private String userName;

    private String userAddress;

    private String userPhone;

    private Integer rate;

    private String comment;

    private String reply;

    private String status;

    @TableField(exist = false)
    private Integer addressId;

    @TableField(exist = false)
    private List<Cart> carts;

}
