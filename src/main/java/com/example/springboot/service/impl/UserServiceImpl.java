package com.example.springboot.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.springboot.common.Constants;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.User;

import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IUserService;

import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public Account login(Account account) throws ServiceException{
        //登录，通过账号密码，查询是否有这条数据
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,account.getUsername());
        wrapper.eq(User::getPassword,account.getPassword());
        User one =userMapper.selectOne(wrapper);
        //如果one==null，说明没找到，即账号密码错误
        if(one != null){
            //找到了,处理一下数据再放回到controlloer层
            String role = "ROLE_USER";
            BeanUtils.copyProperties(one,account);
            String token = TokenUtils.createToken( one.getId() + "-" + role, account.getPassword());
            account.setToken(token);
            account.setRole(role);
            account.setPassword(null);
            return account;
        }else{
            throw new ServiceException(Constants.CODE_605,"用户名或密码错误");
        }

    }

    @Override
    public void register(Account account) {

        //先保证注册前，用户名不重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,account.getUsername());
        wrapper.eq(User::getPassword,account.getPassword());
        User one =userMapper.selectOne(wrapper);
        //为空说明，可以注册
        if(one == null){
            one = new User();
            //bean工具，拷贝属性
            BeanUtils.copyProperties(account,one);
            userMapper.insert(one);
        }else{
            //不空说明已经有了这个账户
            throw new ServiceException(Constants.CODE_605,"用户已存在");
        }
    }

    @Override
    public void updatePassword(Account account) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUsername, account.getUsername());
        wrapper.eq(User::getPassword, account.getPassword());
        wrapper.set(User::getPassword, account.getNewPassword());
        // 执行更新操作
        int updateCount = userMapper.update(null, wrapper);
        // 检查更新结果
        if (updateCount == 0) {
            throw new ServiceException(Constants.CODE_605, "原密码输入错误，请检查后再试！");
        }
    }

}
