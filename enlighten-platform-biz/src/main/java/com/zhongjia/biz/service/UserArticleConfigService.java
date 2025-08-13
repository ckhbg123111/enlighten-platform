package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.UserArticleConfig;

import java.util.List;
import java.util.Map;

public interface UserArticleConfigService {

    List<UserArticleConfig> listByUserId(Long userId);

    List<UserArticleConfig> listByUserIdAndCategory(Long userId, String category);

    boolean removeByUserIdAndOptionCode(Long userId, String optionCode);

    /**
     * 根据用户获取分组后的选项；若为空则负责生成并存储默认选项后再返回。
     */
    Map<String, List<UserArticleConfig>> loadOrInitByUser(Long userId);

    UserArticleConfig getByUserIdAndOptionCode(Long userId, String optionCode);

    boolean updateById(UserArticleConfig entity);

    boolean save(UserArticleConfig entity);
}


