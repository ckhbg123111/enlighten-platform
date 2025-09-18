package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 素材实体类
 */
@Data
@Accessors(chain = true)
@TableName("typesetting_material")
public class TypesettingMaterial {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 素材名称
     */
    private String name;
    
    /**
     * 所属医院
     */
    private String hospital;
    
    /**
     * 所属科室
     */
    private String department;
    
    /**
     * 顺序
     */
    private Integer sort;
    
    /**
     * 素材类型
     */
    private String type;
    
    /**
     * 素材内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted;
}
