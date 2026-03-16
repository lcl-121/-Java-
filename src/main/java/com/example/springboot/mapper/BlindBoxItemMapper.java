package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.BlindBoxItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盲盒商品项 Mapper
 */
@Mapper
public interface BlindBoxItemMapper extends BaseMapper<BlindBoxItem> {
}
