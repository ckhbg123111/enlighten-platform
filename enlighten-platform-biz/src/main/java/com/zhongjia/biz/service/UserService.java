package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
    
    /**
     * 根据状态查询用户列表
     */
    List<User> getByStatus(Integer status);
    
    /**
     * 批量更新用户状态
     */
    boolean updateStatusByIds(List<Long> ids, Integer status);

    User getById(Long id);

    boolean updateById(User user);

    boolean removeById(Long id);

    boolean save(User user);

    java.util.List<User> list();

    Page<User> page(
            Page<User> page,
            LambdaQueryWrapper<User> wrapper);

    long count();
}
