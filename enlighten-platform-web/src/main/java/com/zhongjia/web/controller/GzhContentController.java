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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<Map<String, Long>> createArticle(
            @RequestParam(required = false) Long folderId,
            @RequestParam String name,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String coverImageUrl,
            @RequestParam(required = false) String originalText,
            @RequestParam(required = false) String typesetContent
    ) {
        Long userId = requireUser().userId();
        Long id = articleService.createInitial(userId, folderId, name, tag, coverImageUrl, originalText, typesetContent);
        return id == null ? Result.error(ErrorCode.INTERNAL_ERROR, "创建失败") : Result.success(Map.of("id", id));
    }

    @PostMapping("/article/update")
    @Operation(summary = "更新文章（状态改为编辑中并更新最后编辑时间）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> updateArticle(
            @RequestParam Long id,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String coverImageUrl,
            @RequestParam(required = false) String originalText,
            @RequestParam(required = false) String typesetContent
    ) {
        Long userId = requireUser().userId();
        return Result.success(articleService.updateEditing(userId, id, folderId, name, tag, coverImageUrl, originalText, typesetContent));
    }

    @PostMapping("/article/delete")
    @Operation(summary = "软删除文章（入回收站）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> deleteArticle(@RequestParam Long id) {
        Long userId = requireUser().userId();
        return Result.success(articleService.softDelete(userId, id));
    }

    @PostMapping("/article/delete-batch")
    @Operation(summary = "批量软删除文章（入回收站）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> deleteArticleBatch(@RequestBody List<Long> ids) {
        Long userId = requireUser().userId();
        return Result.success(articleService.batchSoftDelete(userId, ids));
    }

    @PostMapping("/article/status-batch")
    @Operation(summary = "批量更新文章状态", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> updateStatusBatch(@RequestBody Map<String, Object> body) {
        Long userId = requireUser().userId();
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        String status = (String) body.get("status");
        return Result.success(articleService.batchUpdateStatus(userId, ids, status));
    }

    @PostMapping("/article/move-batch")
    @Operation(summary = "批量移动到文件夹", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> moveBatch(@RequestBody Map<String, Object> body) {
        Long userId = requireUser().userId();
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        Long folderId = ((Number) body.get("folderId")).longValue();
        return Result.success(articleService.batchMoveToFolder(userId, ids, folderId));
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
}


