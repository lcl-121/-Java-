package com.example.springboot.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RelateDTO {
    /** 用户id */
    private Integer useId;
    /** 物品id */
    private Integer itemId;
    /** 指数 */
    private Integer index;

}