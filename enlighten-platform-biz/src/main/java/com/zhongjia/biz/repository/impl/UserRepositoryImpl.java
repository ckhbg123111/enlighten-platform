package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.User;
import com.zhongjia.biz.mapper.UserMapper;
import com.zhongjia.biz.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
}


