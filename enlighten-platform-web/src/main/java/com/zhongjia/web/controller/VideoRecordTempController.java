package com.zhongjia.web.controller;

import com.zhongjia.biz.service.VideoRecordTempService;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.VideoRecordTempVO;
import com.zhongjia.web.mapper.VideoRecordTempWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "视频记录临时接口")
@RequestMapping("/api/video-temp")
public class VideoRecordTempController {

    @Autowired
    private VideoRecordTempService service;

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

    @PostMapping("/insert")
    @Operation(summary = "插入一条视频临时记录", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Map<String, Long>> insert(@Valid @RequestBody InsertReq req) {
        Long userId = requireUser().userId();
        Long id = service.insert(userId, req.getTaskId(), req.getStatus(), req.getUrl(), req.getStepList());
        if (id == null) {
            return Result.error(ErrorCode.INTERNAL_ERROR, "插入失败");
        }
        return Result.success(Map.of("id", id));
    }

    @PostMapping("/update")
    @Operation(summary = "更新状态/地址/步骤（按ID）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> update(@Valid @RequestBody UpdateReq req) {
        Long userId = requireUser().userId();
        boolean ok = service.updateStatusAndUrl(userId, req.getId(), req.getStatus(), req.getUrl(), req.getStepList());
        return ok ? Result.success(true) : Result.error(ErrorCode.INTERNAL_ERROR, "更新失败");
    }

    @GetMapping("/list")
    @Operation(summary = "按用户查询列表，按时间倒序", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<List<VideoRecordTempVO>> list(@RequestParam(value = "limit", required = false) Integer limit,
                                             @RequestParam(value = "lastId", required = false) Long lastId) {
        Long userId = requireUser().userId();
        java.util.List<com.zhongjia.biz.entity.VideoRecordTemp> list = service.listByUserOrderByTimeDesc(userId, limit, lastId);
        return Result.success(VideoRecordTempWebMapper.toVOList(list));
    }

    @Data
    @Schema(name = "VideoRecordTempInsertReq", description = "插入视频临时记录请求")
    public static class InsertReq {
        @NotBlank
        private String taskId;
        @NotBlank
        private String status;
        private String url;
        private java.util.List<Object> stepList;
    }

    @Data
    @Schema(name = "VideoRecordTempUpdateReq", description = "更新视频临时记录请求")
    public static class UpdateReq {
        @NotNull
        private Long id;
        private String status;
        private String url;
        private java.util.List<Object> stepList;
    }
}


