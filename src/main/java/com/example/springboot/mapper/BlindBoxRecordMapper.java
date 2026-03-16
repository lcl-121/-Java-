package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.BlindBoxRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盲盒参与记录 Mapper
 */
@Mapper
public interface BlindBoxRecordMapper extends BaseMapper<BlindBoxRecord> {
}
