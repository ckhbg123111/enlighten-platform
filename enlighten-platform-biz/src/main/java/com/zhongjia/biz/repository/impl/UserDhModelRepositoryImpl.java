package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.UserDhModel;
import com.zhongjia.biz.mapper.UserDhModelMapper;
import com.zhongjia.biz.repository.UserDhModelRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserDhModelRepositoryImpl extends ServiceImpl<UserDhModelMapper, UserDhModel> implements UserDhModelRepository {
}


