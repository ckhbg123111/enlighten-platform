package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.ScienceGenRecord;
import com.zhongjia.biz.service.ScienceGenRecordService;
import com.zhongjia.web.security.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/science-generator")
public class ScienceGeneratorController {

	@Autowired
	private ScienceGenRecordService recordService;

	@Value("${app.upstream.science-generator-url:http://192.168.1.65:8000/science-generator}")
	private String upstreamUrl;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public void streamGenerate(@Valid @RequestBody GenerateReq req, HttpServletResponse response) throws IOException {
		UserContext.UserInfo user = requireUser();

		ScienceGenRecord record = new ScienceGenRecord()
			.setUserId(user.userId())
			.setTenantId(user.tenantId())
			.setCode(req.getCode())
			.setReqBody(req.buildUpstreamBody())
			.setCreateTime(LocalDateTime.now());
		recordService.save(record);

		response.setStatus(200);
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.flushBuffer();

		StringBuilder respBuf = new StringBuilder(256);
		try {
			streamUpstream(record.getReqBody(), response, respBuf);
			record.setSuccess(true).setRespContent(respBuf.toString());
		} catch (Exception ex) {
			record.setSuccess(false).setErrorMessage(ex.getMessage());
			writeSseLine(response, "data:{\"code\":500,\"success\":false,\"msg\":\"服务异常！\",\"data\":null}\n\n");
		} finally {
			recordService.updateById(record);
		}
	}

	private void streamUpstream(String upstreamBody, HttpServletResponse resp, StringBuilder respBuf) throws Exception {
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(upstreamUrl))
				.timeout(Duration.ofMinutes(10))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(upstreamBody, StandardCharsets.UTF_8))
				.build();

		HttpResponse<java.io.InputStream> upstream = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (upstream.statusCode() != 200) {
			throw new IllegalStateException("上游返回非200:" + upstream.statusCode());
		}
		try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(upstream.body(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("data:")) {
					writeSseLine(resp, line + "\n\n");
					respBuf.append(extractContentDelta(line));
				}
			}
		}
	}

	private static void writeSseLine(HttpServletResponse response, String s) throws IOException {
		response.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
		response.flushBuffer();
	}

	private UserContext.UserInfo requireUser() {
		UserContext.UserInfo info = UserContext.get();
		if (info == null || info.userId() == null) throw new RuntimeException("未认证");
		return info;
	}

	private static String extractContentDelta(String dataLine) {
		int idx = dataLine.indexOf("\"content\":");
		if (idx < 0) return "";
		int start = dataLine.indexOf('"', idx + 10);
		if (start < 0) return "";
		int end = dataLine.indexOf('"', start + 1);
		if (end < 0) return "";
		String piece = dataLine.substring(start + 1, end);
		return piece.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
	}

	@Data
	public static class GenerateReq {
		private String document_id;
		private Boolean contains_image;
		@NotBlank
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


