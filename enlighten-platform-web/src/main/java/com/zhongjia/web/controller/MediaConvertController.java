package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.enums.MediaPlatform;
import com.zhongjia.biz.service.GzhArticleService;
import com.zhongjia.biz.entity.MediaConvertRecordV2;
import com.zhongjia.biz.service.MediaConvertRecordV2Service;
import com.zhongjia.biz.repository.MediaConvertRecordV2Repository;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.web.vo.PageResponse;
import com.zhongjia.biz.service.MediaConvertRecordService;
import com.zhongjia.biz.service.dto.UpstreamResult;
import com.zhongjia.biz.repository.MediaConvertRecordRepository;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.MediaConvertRecordVO;
import com.zhongjia.web.mapper.MediaConvertRecordMapper;
import com.zhongjia.web.vo.MediaConvertRecordV2VO;
import com.zhongjia.web.mapper.MediaConvertRecordV2WebMapper;
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
import com.zhongjia.biz.service.ArticleStructureService;
import com.zhongjia.biz.service.TemplateApplyService;
import com.zhongjia.biz.service.dto.ArticleStructure;
import jakarta.validation.constraints.NotNull;

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
    private MediaConvertRecordV2Repository recordV2Repository;

	@Autowired
	private MediaConvertRecordV2WebMapper recordV2WebMapper;

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

    // 4) 转公众号图文并套用模板：传入文章内容+模板ID，返回HTML
    @PostMapping(path = "apply_template", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "文章套用模板生成HTML", description = "传入文章与模板ID，返回渲染后的HTML", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<HtmlResp> applyTemplate(@Valid @RequestBody ApplyTemplateReq req) {
        UserContext.UserInfo user = requireUser();
        // 解析结构
        ArticleStructure structure = articleStructureService.parse(req.getEssay());
        // 渲染
        String html = templateApplyService.render(req.getTemplateId(), structure);
        // 若传入记录ID，尝试将原文与渲染结果写回对应记录
        if (req.getRecordId() != null) {
            GzhArticle record = gzhArticleService.getById(req.getRecordId());
            if (record != null && (record.getDeleted() == null || record.getDeleted() == 0)
                    && record.getUserId() != null && record.getUserId().equals(user.userId())) {
                // 使用 updateEditing 以便同时更新状态与最后编辑时间
                try {
                    gzhArticleService.updateEditing(
                            user.userId(),
                            record.getId(),
                            null,
                            null,
                            null,
                            null,
                            req.getEssay(), // originalText
                            html            // typesetContent
                    );
                } catch (Exception ignored) {
                    // 按要求：记录不存在或更新失败都不影响正常返回
                }
            }
        }
        HtmlResp resp = new HtmlResp();
        resp.setHtml(html);
        return Result.success(resp);
    }

    // 4.1) 转公众号图文并套用模板：传入公众号内容记录ID+模板ID，返回HTML
    @PostMapping(path = "apply_template_by_record", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "文章套用模板生成HTML-通过记录ID", description = "传入公众号内容记录ID与模板ID，返回渲染后的HTML", security = {@SecurityRequirement(name = "bearer-jwt")})
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
        // v2：转换前插入一条记录
        MediaConvertRecordV2 v2 = recordV2Service.insertProcessing(user.userId(), req.getRecordId(), "gzh");
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
                recordV2Service.markFailed(v2.getId());
                return Result.error(500, "更新文章失败");
            }
            // 更新状态成功
            recordV2Service.markSuccess(v2.getId());
            HtmlResp resp = new HtmlResp();
            resp.setHtml(html);
            return Result.success(resp);
        } catch (Exception e) {
            // 更新状态失败
            recordV2Service.markFailed(v2.getId());
            throw e;
        }
    }

    // v2) 查询接口：按 platform + user_id 查询最近记录
    @GetMapping(path = "records_v2")
    @Operation(summary = "查询媒体转换记录v2", description = "根据平台查询当前用户的转换记录（可分页，platform 可为空）", security = {@SecurityRequirement(name = "bearer-jwt")})
	public Result<PageResponse<MediaConvertRecordV2VO>> listV2(
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserContext.UserInfo user = requireUser();
        LambdaQueryWrapper<MediaConvertRecordV2> qw = new LambdaQueryWrapper<MediaConvertRecordV2>()
                .eq(MediaConvertRecordV2::getUserId, user.userId())
                .eq(MediaConvertRecordV2::getDeleted, 0);
        if (platform != null && !platform.isEmpty()) {
            if (!MediaPlatform.isValid(platform)) {
                return Result.error(400, "platform不合法");
            }
            qw.eq(MediaConvertRecordV2::getPlatform, platform);
        }
        qw.orderByDesc(MediaConvertRecordV2::getCreateTime);
        Page<MediaConvertRecordV2> p = new Page<>(page, size);
        Page<MediaConvertRecordV2> result = recordV2Repository.page(p, qw);
		PageResponse<MediaConvertRecordV2VO> resp = PageResponse.of((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), recordV2WebMapper.toVOList(result.getRecords()));
        return Result.success(resp);
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
}


