package com.example.springboot.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Banner;
import com.example.springboot.entity.Cart;
import com.example.springboot.mapper.BannerMapper;
import com.example.springboot.mapper.CartMapper;
import com.example.springboot.service.IBannerService;
import com.example.springboot.service.ICartService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {



}
