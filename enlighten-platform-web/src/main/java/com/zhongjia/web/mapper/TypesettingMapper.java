package com.zhongjia.web.mapper;

import com.zhongjia.biz.entity.TypesettingTemplate;
import com.zhongjia.biz.entity.TypesettingMaterial;
import com.zhongjia.web.vo.TypesettingTemplateInfoVO;
import com.zhongjia.web.vo.TypesettingMaterialVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 排版Mapper类
 */
@Component
public class TypesettingMapper {
    
    /**
     * 模板实体转VO
     */
    public TypesettingTemplateInfoVO toTemplateVO(TypesettingTemplate template) {
        if (template == null) {
            return null;
        }
        TypesettingTemplateInfoVO vo = new TypesettingTemplateInfoVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }
    
    /**
     * 素材实体转VO
     */
    public TypesettingMaterialVO toMaterialVO(TypesettingMaterial material) {
        if (material == null) {
            return null;
        }
        TypesettingMaterialVO vo = new TypesettingMaterialVO();
        BeanUtils.copyProperties(material, vo);
        return vo;
    }
}
