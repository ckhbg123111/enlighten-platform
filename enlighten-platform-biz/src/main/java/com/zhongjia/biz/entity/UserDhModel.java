package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("user_dh_model")
public class UserDhModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String modelName;

    private LocalDateTime createTime;
}


