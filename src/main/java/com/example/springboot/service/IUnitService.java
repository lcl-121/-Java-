package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Unit;
import com.example.springboot.entity.User;


/**
 * <p>
 *  服务类
 * </p>
 */
public interface IUnitService extends IService<Unit> {

    Account login(Account account);

    void register(Account account);

    void updatePassword(Account account);
}
