package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.entity.MediaConvertRecordV2;
import com.zhongjia.biz.entity.TypesettingTemplate;
import com.zhongjia.biz.enums.MediaConvertStatus;
import com.zhongjia.biz.enums.MediaPlatform;
import com.zhongjia.biz.repository.MediaConvertRecordRepository;
import com.zhongjia.biz.repository.TypesettingTemplateRepository;
import com.zhongjia.biz.service.*;
import com.zhongjia.biz.service.dto.ArticleStructure;
import com.zhongjia.biz.service.dto.UpstreamResult;
import com.zhongjia.biz.service.mq.MediaConvertTaskMessage;
import com.zhongjia.biz.service.mq.MediaConvertTaskProducer;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.mapper.MediaConvertRecordMapper;
import com.zhongjia.web.mapper.MediaConvertRecordV2WebMapper;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.MediaConvertRecordV2VO;
import com.zhongjia.web.vo.MediaConvertRecordVO;
import com.zhongjia.web.vo.PageResponse;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private ArticleStructureService articleStructureService;

    @Autowired
    private TemplateApplyService templateApplyService;

    @Autowired
    private GzhArticleService gzhArticleService;

    @Autowired
    private MediaConvertRecordV2Service recordV2Service;

    @Autowired
    private MediaConvertRecordV2WebMapper recordV2WebMapper;

    @Autowired
    private MediaConvertTaskProducer mediaConvertTaskProducer;

    @Autowired
    private MediaConvertCancelService cancelService;

    @Autowired
    private TypesettingTemplateRepository tplRespository;


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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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

    // 4) 转公众号图文并套用模板：异步
    @PostMapping(path = "apply_template", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "文章套用模板-异步", description = "立即返回记录ID，异步生成；支持打断与轮询", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<StartResp> applyTemplate(@Valid @RequestBody ApplyTemplateReq req) {
        UserContext.UserInfo user = requireUser();
        if (Objects.isNull(req.getRecordId())) {
            Long initial = gzhArticleService.createInitial(user.userId(), null, null, null, null, req.getEssay(), null);
            req.setRecordId(initial);
        }
        // 查询记录
        GzhArticle record = gzhArticleService.getById(req.getRecordId());// 确保存在且未删除
        if (record == null || (record.getDeleted() != null && record.getDeleted() == 1)) {
            return Result.error(404, "记录不存在");
        }
        // v2：仅插入 PROCESSING 记录并发送任务，立即返回ID
        Long recordV2Id = recordV2Service.insertProcessingRecord(user.userId(), req.getRecordId(), "gzh");
        MediaConvertTaskMessage msg = new MediaConvertTaskMessage();
        msg.setRecordV2Id(recordV2Id);
        msg.setExternalId(req.getRecordId());
        msg.setTemplateId(req.getTemplateId());
        msg.setUserId(user.userId());
        msg.setPlatform("gzh");
        msg.setEssay(req.getEssay());
        String traceId = org.slf4j.MDC.get("traceId");
        msg.setTraceId(traceId);
        mediaConvertTaskProducer.send(msg);

        StartResp resp = new StartResp();
        resp.setId(recordV2Id);
        return Result.success(resp);
    }

    record ReplaceSampleReq(ArticleStructure context, Long tplId){};

    @PostMapping(path = "replace_sample")
    @Deprecated
    public Result<String> replaceSample(@RequestBody ReplaceSampleReq req) {
        String html = templateApplyService.render(req.tplId, req.context);
        TypesettingTemplate t = new  TypesettingTemplate();
        t.setId(req.tplId);
        t.setSample(html);
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        t.setDeleted(0);
        tplRespository.updateById(t);
        return Result.success("成功");
    }

    // 4.1) 转公众号图文并套用模板：传入公众号内容记录ID+模板ID，返回HTML
    @PostMapping(path = "apply_template_by_record", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "文章套用模板生成HTML-通过记录ID", description = "传入公众号内容记录ID与模板ID，返回渲染后的HTML", security = {@SecurityRequirement(name = "bearer-jwt")})
    @Deprecated
    public Result<HtmlResp> applyTemplateByRecord(@Valid @RequestBody ApplyTemplateByRecordReq req) {
        UserContext.UserInfo user = requireUser();
        // 查询记录
        GzhArticle record = gzhArticleService.getById(req.getRecordId());// 确保存在且未删除
        if (record == null || (record.getDeleted() != null && record.getDeleted() == 1)) {
            return Result.error(404, "记录不存在");
        }
        if (record.getOriginalText() == null || record.getOriginalText().isEmpty()) {
            return Result.error(400, "记录内容为空");
        }
        // v2：转换前改为仅插入 PROCESSING 记录
        Long recordV2Id = recordV2Service.insertProcessingRecord(user.userId(), req.getRecordId(), "gzh");
        // 解析结构
        try {
            ArticleStructure structure = articleStructureService.parse(record.getOriginalText());
            // 渲染
            String html = templateApplyService.render(req.getTemplateId(), structure);
            // 将结果更新落库：typesetContent、状态为编辑中、最后编辑时间
            boolean saved = gzhArticleService.updateEditing(
                    user.userId(),
                    record.getId(),
                    null, // folderId 不变
                    null, // name 不变
                    null, // tag 不变
                    null, // coverImageUrl 不变
                    null, // originalText 不变
                    html  // 更新 typesetContent
            );
            if (!saved) {
                // 若更新失败，标记失败并返回
                recordV2Service.markFailed(recordV2Id, record.getOriginalText());
                return Result.error(500, "更新文章失败");
            }
            // 更新状态成功，同时写入原文与生成内容
            recordV2Service.markSuccess(recordV2Id, record.getOriginalText(), html);
            HtmlResp resp = new HtmlResp();
            resp.setHtml(html);
            return Result.success(resp);
        } catch (Exception e) {
            // 更新状态失败，同时保存原文
            recordV2Service.markFailed(recordV2Id, record.getOriginalText());
            return Result.error(500, e.getMessage());
        }
    }

    // v2) 查询接口：按 platform + user_id 查询最近记录
    @GetMapping(path = "records_v2")
    @Operation(summary = "查询媒体转换记录v2", description = "根据平台查询当前用户的转换记录（可分页，platform 可为空）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<PageResponse<MediaConvertRecordV2VO>> listV2(
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "statuses", required = false) java.util.List<MediaConvertStatus> statuses,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserContext.UserInfo user = requireUser();
        if (platform != null && !platform.isEmpty()) {
            if (!MediaPlatform.isValid(platform)) {
                return Result.error(400, "platform不合法");
            }
        }
        Page<MediaConvertRecordV2> result = recordV2Service.pageRecords(user.userId(), platform, statuses, page, size);
        PageResponse<MediaConvertRecordV2VO> resp = PageResponse.of((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), recordV2WebMapper.toVOList(result.getRecords()));
        return Result.success(resp);
    }

    // v2) 查询单条记录（轮询）
    @GetMapping(path = "records_v2/{id}")
    @Operation(summary = "查询单条媒体转换记录v2", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<MediaConvertRecordV2VO> getOneV2(@Parameter(description = "记录ID") @PathVariable("id") Long id) {
        UserContext.UserInfo user = requireUser();
        MediaConvertRecordV2 record = recordV2Service.getById(id);
        if (record == null || record.getDeleted() != null && record.getDeleted() == 1) {
            return Result.error(404, "记录不存在");
        }
        if (!record.getUserId().equals(user.userId())) {
            return Result.error(403, "无权限");
        }
        return Result.success(recordV2WebMapper.toVO(record));
    }

    // v2) 打断任务
    @PostMapping(path = "records_v2/{id}/cancel")
    @Operation(summary = "打断媒体转换任务v2", description = "PROCESSING状态下设置取消标记，并标记为已打断", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> cancelV2(@Parameter(description = "记录ID") @PathVariable("id") Long id) {
        UserContext.UserInfo user = requireUser();
        MediaConvertRecordV2 record = recordV2Service.getById(id);
        if (record == null || record.getDeleted() != null && record.getDeleted() == 1) {
            return Result.error(404, "记录不存在");
        }
        if (!record.getUserId().equals(user.userId())) {
            return Result.error(403, "无权限");
        }
        if (!"PROCESSING".equals(record.getStatus())) {
            return Result.success(true);
        }
        cancelService.cancel(id);
        String original = null;
        GzhArticle gzh = gzhArticleService.getById(record.getExternalId());
        if (gzh != null) original = gzh.getOriginalText();
        recordV2Service.markInterrupted(id, original);
        return Result.success(true);
    }

    // v2) 删除：软删除
    @DeleteMapping(path = "records_v2/{id}")
    @Operation(summary = "删除媒体转换记录v2(软删除)", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> softDeleteV2(@Parameter(description = "记录ID") @PathVariable("id") Long id) {
        UserContext.UserInfo user = requireUser();
        MediaConvertRecordV2Service.SoftDeleteResult r = recordV2Service.softDeleteById(user.userId(), id);
        switch (r) {
            case SUCCESS:
                return Result.success(true);
            case FORBIDDEN:
                return Result.error(403, "无权限");
            case NOT_FOUND:
            case ALREADY_DELETED:
                return Result.error(404, "记录不存在");
            case FAILED:
            default:
                return Result.error(500, "删除失败");
        }
    }

    @Data
    @Schema(name = "ApplyTemplateReq", description = "文章套用模板请求")
    public static class ApplyTemplateReq {
        @Schema(description = "文章原文")
        @NotBlank
        private String essay;
        @Schema(description = "模板ID")
        @NotNull
        private Long templateId;
        @Schema(description = "公众号记录ID，可为空。若存在则会回写原文与排版内容")
        private Long recordId;
        // hospital 和 department 已不再传递至上游，仅保留 essay 和 templateId
    }

    @Data
    @Schema(name = "ApplyTemplateByRecordReq", description = "文章套用模板请求-通过记录ID")
    public static class ApplyTemplateByRecordReq {
        @Schema(description = "公众号记录ID")
        @NotNull
        private Long recordId;
        @Schema(description = "模板ID")
        @NotNull
        private Long templateId;
    }

    @Data
    @Schema(name = "HtmlResp", description = "渲染结果")
    public static class HtmlResp {
        private String html;
    }

    @Data
    @Schema(name = "StartResp", description = "异步任务启动响应")
    public static class StartResp {
        private Long id;
    }
}


