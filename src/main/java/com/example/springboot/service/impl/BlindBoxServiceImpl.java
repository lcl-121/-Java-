package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.BlindBox;
import com.example.springboot.mapper.BlindBoxMapper;
import com.example.springboot.service.IBlindBoxService;
import org.springframework.stereotype.Service;

/**
 * 盲盒活动服务实现类
 */
@Service
public class BlindBoxServiceImpl extends ServiceImpl<BlindBoxMapper, BlindBox> implements IBlindBoxService {
}
