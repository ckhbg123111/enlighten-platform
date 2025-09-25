package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhongjia.biz.entity.VideoRecordTemp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.repository.VideoRecordTempRepository;
import com.zhongjia.biz.service.VideoRecordTempService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoRecordTempServiceImpl implements VideoRecordTempService {

    @Autowired
    private VideoRecordTempRepository repository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Long insert(Long userId, String taskId, String status, String url, java.util.List<Object> stepList) {
        VideoRecordTemp po = new VideoRecordTemp();
        po.setUserId(userId);
        po.setTaskId(taskId);
        po.setStatus(status);
        po.setUrl(url);
        if (stepList != null) {
            try {
                po.setStepList(MAPPER.writeValueAsString(stepList));
            } catch (Exception e) {
                // 简化处理：序列化失败则置空
                po.setStepList(null);
            }
        }
        po.setDeleted(0);
        boolean ok = repository.save(po);
        return ok ? po.getId() : null;
    }

    @Override
    public boolean updateStatusAndUrl(Long userId, Long id, String status, String url, java.util.List<Object> stepList) {
        LambdaUpdateWrapper<VideoRecordTemp> uw = new LambdaUpdateWrapper<>();
        uw.eq(VideoRecordTemp::getId, id)
          .eq(VideoRecordTemp::getUserId, userId)
          .set(status != null, VideoRecordTemp::getStatus, status)
          .set(url != null, VideoRecordTemp::getUrl, url)
          .set(stepList != null, VideoRecordTemp::getStepList, serialize(stepList));
        return repository.update(uw);
    }

    @Override
    public List<VideoRecordTemp> listByUserOrderByTimeDesc(Long userId, Integer limit, Long lastId) {
        LambdaQueryWrapper<VideoRecordTemp> qw = new LambdaQueryWrapper<>();
        qw.eq(VideoRecordTemp::getUserId, userId)
          .eq(VideoRecordTemp::getDeleted, 0)
          .orderByDesc(VideoRecordTemp::getCreateTime);
        if (lastId != null) {
            // 采用 id 游标倒序，假设 id 自增与时间近似相关
            qw.lt(VideoRecordTemp::getId, lastId);
        }
        if (limit != null && limit > 0) {
            qw.last("LIMIT " + Math.min(limit, 100));
        } else {
            qw.last("LIMIT 50");
        }
        return repository.list(qw);
    }

    private String serialize(java.util.List<Object> stepList) {
        if (stepList == null) return null;
        try {
            return MAPPER.writeValueAsString(stepList);
        } catch (Exception e) {
            return null;
        }
    }
}


