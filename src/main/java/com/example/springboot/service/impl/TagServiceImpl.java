package com.example.springboot.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Tag;
import com.example.springboot.entity.Type;
import com.example.springboot.mapper.TagMapper;
import com.example.springboot.mapper.TypeMapper;
import com.example.springboot.service.ITagService;
import com.example.springboot.service.ITypeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {



}
