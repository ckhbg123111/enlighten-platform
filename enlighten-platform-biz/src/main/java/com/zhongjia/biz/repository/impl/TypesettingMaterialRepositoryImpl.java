package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.TypesettingMaterial;
import com.zhongjia.biz.mapper.TypesettingMaterialMapper;
import com.zhongjia.biz.repository.TypesettingMaterialRepository;
import org.springframework.stereotype.Repository;

/**
 * 素材Repository实现类
 */
@Repository
public class TypesettingMaterialRepositoryImpl extends ServiceImpl<TypesettingMaterialMapper, TypesettingMaterial> implements TypesettingMaterialRepository {
}
