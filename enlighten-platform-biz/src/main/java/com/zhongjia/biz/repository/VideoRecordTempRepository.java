package com.zhongjia.biz.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongjia.biz.entity.VideoRecordTemp;

public interface VideoRecordTempRepository extends IService<VideoRecordTemp> {
    Long countAllByUserId(Long userId);
}


