package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.enums.MediaPlatform;
import com.zhongjia.biz.service.MediaConvertRecordService;
import com.zhongjia.biz.service.dto.UpstreamResult;
import com.zhongjia.biz.repository.MediaConvertRecordRepository;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.MediaConvertRecordVO;
import com.zhongjia.web.mapper.MediaConvertRecordMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "媒体内容转换")
@RequestMapping("/api/convert2media")
public class MediaConvertController {

    @Autowired
    private MediaConvertRecordService recordService;

    @Autowired
    private MediaConvertRecordRepository recordRepository;

    @Autowired
    private MediaConvertRecordMapper mediaConvertRecordMapper;

    // 上游调用与记录统一移至 Service 层

    // 1) 转换：解析上游，并记录
    @PostMapping(path = "common", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "通用媒体转换", security = {@SecurityRequirement(name = "bearer-jwt")})
    public void convert(@Valid @RequestBody ConvertReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        if (!MediaPlatform.isValid(req.getPlatform())) {
            writeJson(response, 400, false, "platform不合法！", null);
            return;
        }

        UpstreamResult r = recordService.convertCommon(
                user.userId(), user.tenantId(), req.getMediaCode(), req.getEssayCode(), req.getContent(), req.getPlatform());
        writeJson(response, r.getCode(), Boolean.TRUE.equals(r.getSuccess()), r.getMsg(), r.getDataRaw());
    }

    // 1.1) 转公众号：重新生成
    @PostMapping(path = "convert2gzh_re", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "转公众号-重新生成", security = {@SecurityRequirement(name = "bearer-jwt")})
    public void convertToGzhRe(@Valid @RequestBody ConvertGzhReReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        UpstreamResult r = recordService.convertGzhRe(
                user.userId(), user.tenantId(), req.getMediaCode(), req.getEssayCode(), req.getContent());
        writeJson(response, r.getCode(), Boolean.TRUE.equals(r.getSuccess()), r.getMsg(), r.getDataRaw());
    }

    // 1.2) 转公众号：按结构化内容
    @PostMapping(path = "convert2gzh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "转公众号-结构化内容", security = {@SecurityRequirement(name = "bearer-jwt")})
    @Deprecated
    public void convertToGzh(@Valid @RequestBody ConvertGzhReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        String contentStr;
        try {
            contentStr = JSON.writeValueAsString(req.getContent());
        } catch (Exception e) {
            contentStr = String.valueOf(req.getContent());
        }
        UpstreamResult r = recordService.convertGzh(
                user.userId(), user.tenantId(), req.getMediaCode(), req.getEssayCode(), contentStr);
        writeJson(response, r.getCode(), Boolean.TRUE.equals(r.getSuccess()), r.getMsg(), r.getDataRaw());
    }

    // 2) 查询：通过 essayCode，倒序（按时间）
    @GetMapping("/records")
    @Operation(summary = "按文章编码查询转换记录", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<List<MediaConvertRecordVO>> listByEssayCode(@Parameter(description = "文章编码") @RequestParam("essayCode") String essayCode) {
        UserContext.UserInfo user = requireUser();
        List<MediaConvertRecord> list = recordRepository.list(new LambdaQueryWrapper<MediaConvertRecord>()
                .eq(MediaConvertRecord::getUserId, user.userId())
                .eq(MediaConvertRecord::getEssayCode, essayCode)
                .eq(MediaConvertRecord::getDeleted, 0)
                .orderByDesc(MediaConvertRecord::getCreateTime));
        return Result.success(mediaConvertRecordMapper.toVOList(list));
    }

    // 2.1) 查询：通过 userId，限制在当前租户内，倒序（按时间）
    @GetMapping("/records/by-user")
    @Operation(summary = "按用户ID查询转换记录", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<List<MediaConvertRecordVO>> listByUserId() {
        UserContext.UserInfo user = requireUser();
        List<MediaConvertRecord> list = recordRepository.list(new LambdaQueryWrapper<MediaConvertRecord>()
                .eq(MediaConvertRecord::getTenantId, user.tenantId())
                .eq(MediaConvertRecord::getUserId, user.userId())
                .eq(MediaConvertRecord::getDeleted, 0)
                .orderByDesc(MediaConvertRecord::getCreateTime));
        return Result.success(mediaConvertRecordMapper.toVOList(list));
    }

    // 3) 删除：软删除
    @DeleteMapping("/{id}")
    @Operation(summary = "删除转换记录(软删除)", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> softDelete(@Parameter(description = "记录ID") @PathVariable("id") Long id) {
        UserContext.UserInfo user = requireUser();
        MediaConvertRecord exist = recordRepository.getById(id);
        if (exist == null || exist.getDeleted() != null && exist.getDeleted() == 1) {
            return Result.error(404, "记录不存在");
        }
        if (!exist.getUserId().equals(user.userId())) {
            return Result.error(403, "无权限");
        }
        exist.setDeleted(1).setDeleteTime(LocalDateTime.now());
        boolean ok = recordRepository.updateById(exist);
        return ok ? Result.success(true) : Result.error(500, "删除失败");
    }

    private static void writeJson(HttpServletResponse response, int code, boolean success, String msg, Object data) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        String payload = String.format("{\"code\":%d,\"success\":%s,\"msg\":\"%s\",\"data\":%s}",
                code,
                success ? "true" : "false",
                escapeJson(msg == null ? "" : msg),
                data == null ? "null" : data.toString());
        response.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

    

    @Data
    @Schema(name = "ConvertCommonReq", description = "通用媒体转换请求")
    public static class ConvertReq {
        @Schema(description = "输入内容")
        @NotBlank
        private String content;
        @Schema(description = "目标平台：xiaohongshu/douyin")
        @NotBlank
        private String platform; // xiaohongshu / douyin
        @Schema(description = "文章编码")
        @NotBlank
        private String essayCode; // 额外字段
        @Schema(description = "媒体唯一编码 uuid")
        @NotBlank
        private String mediaCode; // 媒体唯一编码 uuid
    }

    @Data
    @Schema(name = "ConvertGzhReReq", description = "转公众号-重新生成 请求")
    public static class ConvertGzhReReq {
        @Schema(description = "输入内容")
        @NotBlank
        private String content;
        @Schema(description = "文章编码")
        @NotBlank
        private String essayCode; // 额外字段
        @Schema(description = "媒体唯一编码 uuid")
        @NotBlank
        private String mediaCode; // 媒体唯一编码 uuid
    }

    @Data
    @Schema(name = "ConvertGzhReq", description = "转公众号-结构化内容 请求")
    public static class ConvertGzhReq {
        @jakarta.validation.constraints.NotNull
        @Schema(description = "结构化内容(数组/对象)")
        private Object content; // 前端解析后的结构化内容 list[dict]
        @Schema(description = "文章编码")
        @NotBlank
        private String essayCode; // 额外字段
        @Schema(description = "媒体唯一编码 uuid")
        @NotBlank
        private String mediaCode; // 媒体唯一编码 uuid
    }
}


