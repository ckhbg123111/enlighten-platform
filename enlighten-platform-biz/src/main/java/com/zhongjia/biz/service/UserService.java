package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongjia.biz.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
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
}
