package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.BlindBox;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盲盒活动 Mapper
 */
@Mapper
public interface BlindBoxMapper extends BaseMapper<BlindBox> {
}
