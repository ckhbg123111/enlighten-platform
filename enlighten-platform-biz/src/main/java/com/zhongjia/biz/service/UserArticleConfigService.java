package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongjia.biz.entity.UserArticleConfig;

import java.util.List;

public interface UserArticleConfigService extends IService<UserArticleConfig> {

    List<UserArticleConfig> listByUserId(Long userId);

    List<UserArticleConfig> listByUserIdAndCategory(Long userId, String category);

    boolean removeByUserIdAndOptionCode(Long userId, String optionCode);
}


