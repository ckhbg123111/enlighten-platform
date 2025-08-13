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

@RestController
@RequestMapping("/api/convert2media")
public class MediaConvertController {

    @Autowired
    private MediaConvertRecordService recordService;

    @Autowired
    private MediaConvertRecordRepository recordRepository;

    // 上游调用与记录统一移至 Service 层

    // 1) 转换：解析上游，并记录
    @PostMapping(path = "common", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
    public void convertToGzhRe(@Valid @RequestBody ConvertGzhReReq req, HttpServletResponse response) throws IOException {
        UserContext.UserInfo user = requireUser();

        UpstreamResult r = recordService.convertGzhRe(
                user.userId(), user.tenantId(), req.getMediaCode(), req.getEssayCode(), req.getContent());
        writeJson(response, r.getCode(), Boolean.TRUE.equals(r.getSuccess()), r.getMsg(), r.getDataRaw());
    }

    // 1.2) 转公众号：按结构化内容
    @PostMapping(path = "convert2gzh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
    public Result<List<MediaConvertRecord>> listByEssayCode(@RequestParam("essayCode") String essayCode) {
        UserContext.UserInfo user = requireUser();
        List<MediaConvertRecord> list = recordRepository.list(new LambdaQueryWrapper<MediaConvertRecord>()
                .eq(MediaConvertRecord::getUserId, user.userId())
                .eq(MediaConvertRecord::getEssayCode, essayCode)
                .eq(MediaConvertRecord::getDeleted, 0)
                .orderByDesc(MediaConvertRecord::getCreateTime));
        return Result.success(list);
    }

    // 3) 删除：软删除
    @DeleteMapping("/{id}")
    public Result<Boolean> softDelete(@PathVariable("id") Long id) {
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
    public static class ConvertReq {
        @NotBlank
        private String content;
        @NotBlank
        private String platform; // xiaohongshu / douyin
        @NotBlank
        private String essayCode; // 额外字段
        @NotBlank
        private String mediaCode; // 媒体唯一编码 uuid
    }

    @Data
    public static class ConvertGzhReReq {
        @NotBlank
        private String content;
        @NotBlank
        private String essayCode; // 额外字段
        @NotBlank
        private String mediaCode; // 媒体唯一编码 uuid
    }

    @Data
    public static class ConvertGzhReq {
        @jakarta.validation.constraints.NotNull
        private Object content; // 前端解析后的结构化内容 list[dict]
        @NotBlank
        private String essayCode; // 额外字段
        @NotBlank
        private String mediaCode; // 媒体唯一编码 uuid
    }
}


