package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.TypesettingMaterial;

/**
 * 素材服务接口
 */
public interface TypesettingMaterialService {
    
    /**
     * 分页查询素材列表（按用户所在医院和科室过滤）
     * 
     * @param type 素材类型
     * @param hospital 医院名称
     * @param department 科室名称
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<TypesettingMaterial> getMaterialsByTypeAndHospitalAndDepartment(String type, String hospital, String department, int page, int size);
}
