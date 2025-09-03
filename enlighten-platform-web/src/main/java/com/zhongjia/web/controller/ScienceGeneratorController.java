package com.zhongjia.web.controller;

import com.alibaba.fastjson.JSON;
import com.zhongjia.biz.service.ScienceGenRecordService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@Tag(name = "科学文章生成")
@RequestMapping("/api/science-generator")
public class ScienceGeneratorController {

	@Autowired
	private ScienceGenRecordService recordService;

    // 上游交互已下沉到 Service

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式生成科学文章(SSE)", description = "返回 text/event-stream", security = {@SecurityRequirement(name = "bearer-jwt")})
    public void streamGenerate(@Valid @RequestBody GenerateReq req, HttpServletResponse response) throws IOException {
		UserContext.UserInfo user = requireUser();

		response.setStatus(200);
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.flushBuffer();

        String body = req.buildUpstreamBody();
        recordService.streamGenerate(user.userId(), user.tenantId(), req.getCode(), body, line -> {
            try { writeSseLine(response, line); } catch (Exception ignored) {}
        });
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/regenerate")
	@Operation(summary = "文章重新生成(SSE)", description = "返回 text/event-stream", security = {@SecurityRequirement(name = "bearer-jwt")})
	public void regenerate(@Valid @RequestBody RegenerateReq req, HttpServletResponse response) throws IOException {
		UserContext.UserInfo user = requireUser();

		response.setStatus(200);
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.flushBuffer();

		recordService.streamReGenerate(req.getCode(),line -> {
			try { writeSseLine(response, line); } catch (Exception ignored) {}
		});
	}

	private static void writeSseLine(HttpServletResponse response, String s) throws IOException {
		response.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
		response.flushBuffer();
	}

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

	@Data
	@Schema(name = "ScienceRegenerateReq", description = "科学文章重生成请求")
	private static class RegenerateReq {
		@NotBlank
		private String code; // 额外字段：唯一编码
	}

    @Data
    @Schema(name = "ScienceGenerateReq", description = "科学文章生成请求")
    public static class GenerateReq {
		private String document_id;
		private Boolean contains_image;
		private String topic;
		@NotBlank
		private String content;
		private String outline;
		@com.fasterxml.jackson.annotation.JsonProperty("case")
		private String _case;
		@NotBlank
		private String style;
		@NotBlank
		private String length;
		@NotBlank
		private String mode;
		@NotBlank
		private String scene;
		@NotBlank
        @Schema(description = "额外字段：唯一编码")
        private String code; // 额外字段：唯一编码

		public String buildUpstreamBody() {
			StringBuilder sb = new StringBuilder(256);
			sb.append('{');
			appendJson(sb, "document_id", document_id);
			appendJson(sb, "contains_image", contains_image);
			appendJson(sb, "topic", topic);
			appendJson(sb, "content", content);
			appendJson(sb, "outline", outline);
			// API 字段名为 case
			if (_case != null) {
				appendJson(sb, "case", _case);
			}
			appendJson(sb, "style", style);
			appendJson(sb, "length", length);
			appendJson(sb, "mode", mode);
			appendJson(sb, "scene", scene);
			if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
			sb.append('}');
			return sb.toString();
		}

		private static void appendJson(StringBuilder sb, String key, String val) {
			if (val == null) return;
			sb.append('"').append(key).append('"').append(':')
					.append('"').append(escapeJson(val)).append('"').append(',');
		}

		private static void appendJson(StringBuilder sb, String key, Boolean val) {
			if (val == null) return;
			sb.append('"').append(key).append('"').append(':')
					.append(val ? "true" : "false").append(',');
		}

		private static String escapeJson(String s) {
			if (s == null) return "";
			return s.replace("\\", "\\\\")
					.replace("\"", "\\\"")
					.replace("\n", "\\n")
					.replace("\r", "\\r");
		}
	}
}


