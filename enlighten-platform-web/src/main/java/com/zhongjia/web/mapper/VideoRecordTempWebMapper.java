package com.zhongjia.web.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.VideoRecordTemp;
import com.zhongjia.web.vo.VideoRecordTempVO;

import java.util.ArrayList;
import java.util.List;

public class VideoRecordTempWebMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static VideoRecordTempVO toVO(VideoRecordTemp po) {
        if (po == null) return null;
        VideoRecordTempVO vo = new VideoRecordTempVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setTaskId(po.getTaskId());
        vo.setStatus(po.getStatus());
        vo.setUrl(po.getUrl());
        vo.setCreateTime(po.getCreateTime());
        vo.setUpdateTime(po.getUpdateTime());
        if (po.getStepList() != null && !po.getStepList().isEmpty()) {
            try {
                List<Object> list = MAPPER.readValue(po.getStepList(), new TypeReference<List<Object>>(){});
                vo.setStepList(list);
            } catch (Exception e) {
                vo.setStepList(null);
            }
        }
        return vo;
    }

    public static List<VideoRecordTempVO> toVOList(List<VideoRecordTemp> list) {
        if (list == null) return java.util.Collections.emptyList();
        List<VideoRecordTempVO> result = new ArrayList<>(list.size());
        for (VideoRecordTemp item : list) {
            result.add(toVO(item));
        }
        return result;
    }
}


