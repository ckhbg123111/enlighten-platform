package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.TypesettingTemplate;
import com.zhongjia.biz.mapper.TypesettingTemplateMapper;
import com.zhongjia.biz.repository.TypesettingTemplateRepository;
import org.springframework.stereotype.Repository;

/**
 * 模板Repository实现类
 */
@Repository
public class TypesettingTemplateRepositoryImpl extends ServiceImpl<TypesettingTemplateMapper, TypesettingTemplate> implements TypesettingTemplateRepository {
}
