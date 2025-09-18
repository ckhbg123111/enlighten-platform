package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.TypesettingTemplate;

/**
 * 模板服务接口
 */
public interface TypesettingTemplateService {
    
    /**
     * 分页查询模板列表（按用户所在医院和科室过滤）
     * 
     * @param hospital 医院名称
     * @param department 科室名称
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<TypesettingTemplate> getTemplatesByHospitalAndDepartment(String hospital, String department, int page, int size);
}
