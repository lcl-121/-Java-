package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 * 实体类
 * </p>
 */

@Data
@TableName(value = "address")
public class Address {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String phone;

    private String address;

    private Integer userId;

}
