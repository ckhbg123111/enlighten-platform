package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.service.GzhArticleService;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.PageResponse;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "公众号文章")
@RequestMapping("/api/gzh")
public class GzhContentController {

    @Autowired
    private GzhArticleService articleService;

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

    // ========== 公众号文章 ==========
    @PostMapping("/article/create")
    @Operation(summary = "插入文章（状态初始化，标签默认'公众号文章'）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Map<String, Long>> createArticle(@Valid @RequestBody CreateArticleReq req) {
        Long userId = requireUser().userId();
        Long id = articleService.createInitial(userId, req.getFolderId(), req.getName(), req.getTag(), req.getCoverImageUrl(), req.getOriginalText(), req.getTypesetContent());
        return id == null ? Result.error(ErrorCode.INTERNAL_ERROR, "创建失败") : Result.success(Map.of("id", id));
    }

    @PostMapping("/article/update")
    @Operation(summary = "更新文章（状态改为编辑中并更新最后编辑时间）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> updateArticle(@Valid @RequestBody UpdateArticleReq req) {
        Long userId = requireUser().userId();
        return Result.success(articleService.updateEditing(userId, req.getId(), req.getFolderId(), req.getName(), req.getTag(), req.getCoverImageUrl(), req.getOriginalText(), req.getTypesetContent()));
    }

    @PostMapping("/article/delete")
    @Operation(summary = "软删除文章（入回收站）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> deleteArticle(@Valid @RequestBody IdReq req) {
        Long userId = requireUser().userId();
        return Result.success(articleService.softDelete(userId, req.getId()));
    }

    @PostMapping("/article/delete-batch")
    @Operation(summary = "批量软删除文章（入回收站）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> deleteArticleBatch(@Valid @RequestBody IdsReq req) {
        Long userId = requireUser().userId();
        return Result.success(articleService.batchSoftDelete(userId, req.getIds()));
    }

    @PostMapping("/article/status-batch")
    @Operation(summary = "批量更新文章状态", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> updateStatusBatch(@Valid @RequestBody StatusBatchReq req) {
        Long userId = requireUser().userId();
        return Result.success(articleService.batchUpdateStatus(userId, req.getIds(), req.getStatus()));
    }

    @PostMapping("/article/move-batch")
    @Operation(summary = "批量移动到文件夹", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> moveBatch(@Valid @RequestBody MoveBatchReq req) {
        Long userId = requireUser().userId();
        return Result.success(articleService.batchMoveToFolder(userId, req.getIds(), req.getFolderId()));
    }

    @GetMapping("/article/page")
    @Operation(summary = "分页查询文章（未删除）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<PageResponse<GzhArticle>> pageArticles(
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段：last_edit_time/name/create_time") @RequestParam(defaultValue = "last_edit_time") String sortBy,
            @Parameter(description = "升序：true/false") @RequestParam(defaultValue = "false") boolean asc
    ) {
        Long userId = requireUser().userId();
        Page<GzhArticle> p = articleService.pageQuery(userId, folderId, name, tag, status, page, size, sortBy, asc);
        PageResponse<GzhArticle> resp = PageResponse.of(page, size, p.getTotal(), p.getRecords());
        return Result.success(resp);
    }

	@GetMapping("/article/detail")
	@Operation(summary = "按ID查询文章详情（未删除且归属校验）", security = {@SecurityRequirement(name = "bearer-jwt")})
	public Result<GzhArticle> getArticleDetail(@RequestParam @NotNull Long id) {
		Long userId = requireUser().userId();
		GzhArticle article = articleService.getById(id);
		if (article == null || !userId.equals(article.getUserId()) || !Integer.valueOf(0).equals(article.getDeleted())) {
			return Result.error(ErrorCode.NOT_FOUND, "文章不存在");
		}
		return Result.success(article);
	}

    @Data
    @Schema(name = "GzhCreateArticleReq", description = "创建公众号文章请求")
    public static class CreateArticleReq {
        @Schema(description = "文件夹ID")
        private Long folderId;
        @Schema(description = "文章名称", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private String name;
        @Schema(description = "文章标签")
        private String tag;
        @Schema(description = "封面图URL")
        private String coverImageUrl;
        @Schema(description = "原始文本")
        @NotBlank
        private String originalText;
        @Schema(description = "排版内容(HTML)")
        private String typesetContent;
    }

    @Data
    @Schema(name = "GzhUpdateArticleReq", description = "更新公众号文章请求")
    public static class UpdateArticleReq {
        @NotNull
        @Schema(description = "文章ID", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private Long id;
        @Schema(description = "文件夹ID")
        private Long folderId;
        @Schema(description = "文章名称")
        private String name;
        @Schema(description = "文章标签")
        private String tag;
        @Schema(description = "封面图URL")
        private String coverImageUrl;
        @Schema(description = "原始文本")
        private String originalText;
        @Schema(description = "排版内容(HTML)")
        private String typesetContent;
    }

    @Data
    @Schema(name = "IdReq", description = "按ID操作的通用请求")
    public static class IdReq {
        @NotNull
        @Schema(description = "主键ID", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private Long id;
    }

    @Data
    @Schema(name = "IdsReq", description = "批量ID请求")
    public static class IdsReq {
        @NotEmpty
        @Schema(description = "ID 列表", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private List<Long> ids;
    }

    @Data
    @Schema(name = "StatusBatchReq", description = "批量更新状态请求")
    public static class StatusBatchReq {
        @NotEmpty
        @Schema(description = "ID 列表", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private List<Long> ids;
        @NotBlank
        @Schema(description = "状态", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private String status;
    }

    @Data
    @Schema(name = "MoveBatchReq", description = "批量移动到文件夹请求")
    public static class MoveBatchReq {
        @NotEmpty
        @Schema(description = "ID 列表", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private List<Long> ids;
        @NotNull
        @Schema(description = "目标文件夹ID", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private Long folderId;
    }
}


