package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.TypesettingMaterial;
import com.zhongjia.biz.repository.TypesettingMaterialRepository;
import com.zhongjia.biz.service.TypesettingMaterialService;
import org.springframework.stereotype.Service;

/**
 * 素材服务实现类
 */
@Service
public class TypesettingMaterialServiceImpl implements TypesettingMaterialService {
    
    private final TypesettingMaterialRepository materialRepository;
    
    public TypesettingMaterialServiceImpl(TypesettingMaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }
    
    @Override
    public Page<TypesettingMaterial> getMaterialsByTypeAndHospitalAndDepartment(String type, String hospital, String department, int page, int size) {
        LambdaQueryWrapper<TypesettingMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TypesettingMaterial::getType, type)
               .eq(TypesettingMaterial::getHospital, hospital)
               .eq(TypesettingMaterial::getDepartment, department)
               .orderByAsc(TypesettingMaterial::getSort);
        
        Page<TypesettingMaterial> pageRequest = new Page<>(page, size);
        return materialRepository.page(pageRequest, wrapper);
    }
}
