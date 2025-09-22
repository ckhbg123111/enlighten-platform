package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjia.biz.entity.UserArticleConfig;
import com.zhongjia.biz.repository.UserArticleConfigRepository;
import com.zhongjia.biz.service.UserArticleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserArticleConfigServiceImpl implements UserArticleConfigService {

    @Autowired
    private UserArticleConfigRepository userArticleConfigRepository;

    @Override
    public List<UserArticleConfig> listByUserId(Long userId) {
        return userArticleConfigRepository.list(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .orderByAsc(UserArticleConfig::getCategory)
                .orderByAsc(UserArticleConfig::getSort)
                .orderByAsc(UserArticleConfig::getId));
    }

    @Override
    public List<UserArticleConfig> listByUserIdAndCategory(Long userId, String category) {
        return userArticleConfigRepository.list(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .eq(UserArticleConfig::getCategory, category)
                .orderByAsc(UserArticleConfig::getSort)
                .orderByAsc(UserArticleConfig::getId));
    }

    @Override
    public boolean removeByUserIdAndOptionCode(Long userId, String optionCode) {
        return userArticleConfigRepository.remove(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .eq(UserArticleConfig::getOptionCode, optionCode));
    }

    @Override
    public Map<String, List<UserArticleConfig>> loadOrInitByUser(Long userId) {
        List<UserArticleConfig> list = listByUserId(userId);
        if (list.isEmpty()) {
            Map<String, List<String>> defaults = new LinkedHashMap<>();
            defaults.put("style", List.of("亲切口语", "专业严谨", "故事化", "小红书", "公众号"));
            defaults.put("length", List.of("短", "中", "长"));
            defaults.put("mode", List.of("常规文章", "双人对话", "故事案例"));
            defaults.put("scene", List.of("日常科普", "入院须知", "术前指导", "术后康复", "慢病管理"));

            List<UserArticleConfig> seeds = new ArrayList<>();
            defaults.forEach((category, names) -> {
                for (int i = 0; i < names.size(); i++) {
                    UserArticleConfig c = new UserArticleConfig();
                    c.setUserId(userId);
                    c.setCategory(category);
                    c.setOptionName(names.get(i));
                    c.setOptionCode(UUID.randomUUID().toString());
                    c.setSort(i);
                    seeds.add(c);
                }
            });
            if (!seeds.isEmpty()) userArticleConfigRepository.saveBatch(seeds);
            list = listByUserId(userId);
        }
        Map<String, List<UserArticleConfig>> grouped = new LinkedHashMap<>();
        for (UserArticleConfig c : list) {
            grouped.computeIfAbsent(c.getCategory(), k -> new ArrayList<>()).add(c);
        }
        return grouped;
    }

    @Override
    public UserArticleConfig getByUserIdAndOptionCode(Long userId, String optionCode) {
        return userArticleConfigRepository.getOne(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .eq(UserArticleConfig::getOptionCode, optionCode)
                .last("limit 1"));
    }

    @Override
    public boolean updateById(UserArticleConfig entity) {
        return userArticleConfigRepository.updateById(entity);
    }

    @Override
    public boolean save(UserArticleConfig entity) {
        return userArticleConfigRepository.save(entity);
    }
}


