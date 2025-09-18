package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.TypesettingTemplate;
import com.zhongjia.biz.repository.TypesettingTemplateRepository;
import com.zhongjia.biz.service.TypesettingTemplateService;
import org.springframework.stereotype.Service;

/**
 * 模板服务实现类
 */
@Service
public class TypesettingTemplateServiceImpl implements TypesettingTemplateService {
    
    private final TypesettingTemplateRepository templateRepository;
    
    public TypesettingTemplateServiceImpl(TypesettingTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
    
    @Override
    public Page<TypesettingTemplate> getTemplatesByHospitalAndDepartment(String hospital, String department, int page, int size) {
        LambdaQueryWrapper<TypesettingTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TypesettingTemplate::getHospital, hospital)
               .eq(TypesettingTemplate::getDepartment, department)
               .orderByAsc(TypesettingTemplate::getSort);
        
        Page<TypesettingTemplate> pageRequest = new Page<>(page, size);
        return templateRepository.page(pageRequest, wrapper);
    }
}
