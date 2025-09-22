package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjia.biz.entity.User;
import com.zhongjia.biz.repository.UserRepository;
import com.zhongjia.biz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userRepository.getOne(wrapper);
    }

    @Override
    public List<User> getByStatus(Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, status);
        return userRepository.list(wrapper);
    }


    @Override
    public User getById(Long id) {
        return userRepository.getById(id);
    }

    @Override
    public boolean updateById(User user) {
        return userRepository.updateById(user);
    }

    @Override
    public boolean removeById(Long id) {
        return userRepository.removeById(id);
    }

    @Override
    public boolean save(User user) {
        return userRepository.save(user);
    }



}
