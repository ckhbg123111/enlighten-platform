package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.VideoRecordTemp;

import java.util.List;

public interface VideoRecordTempService {

    Long insert(Long userId, String taskId, String status, String url, java.util.List<Object> stepList);

    boolean updateStatusAndUrl(Long userId, Long id, String status, String url, java.util.List<Object> stepList);

    List<VideoRecordTemp> listByUser(Long userId, Integer limit, Long lastId, Boolean asc);
}


