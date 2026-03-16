package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.BlindBoxRecord;

/**
 * 盲盒参与记录服务接口
 */
public interface IBlindBoxRecordService extends IService<BlindBoxRecord> {
    
    /**
     * 分页查询参与记录
     */
    Page<BlindBoxRecord> selectPage(Page<BlindBoxRecord> page, Integer boxId);
}
