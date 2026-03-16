package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盲盒活动实体类
 */
@Data
@TableName("blind_box")
public class BlindBox {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String description;

    private Date startTime;

    private Date endTime;

    private BigDecimal price;

    private Integer status;

    private Integer totalCount;

    private Integer usedCount;

    private Date createTime;

    private Date updateTime;
}
