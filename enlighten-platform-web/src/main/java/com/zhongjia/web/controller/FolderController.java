package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.Folder;
import com.zhongjia.biz.service.FolderService;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "文件夹管理")
@RequestMapping("/api/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件夹", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Map<String, Long>> createFolder(@Valid @RequestBody CreateReq req) {
        Long userId = requireUser().userId();
        Long id = folderService.create(userId, req.getName(), req.getSort());
        return id == null ? Result.error(ErrorCode.INTERNAL_ERROR, "创建失败") : Result.success(Map.of("id", id));
    }

    @PostMapping("/rename")
    @Operation(summary = "重命名文件夹", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> renameFolder(@Valid @RequestBody RenameReq req) {
        Long userId = requireUser().userId();
        return Result.success(folderService.rename(userId, req.getFolderId(), req.getName()));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除文件夹（软删除，文件夹与文章进入回收站）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> deleteFolder(@Valid @RequestBody DeleteReq req) {
        Long userId = requireUser().userId();
        return Result.success(folderService.deleteSoft(userId, req.getFolderId()));
    }

    @GetMapping("/list")
    @Operation(summary = "按顺序查询文件夹列表", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<List<Folder>> listFolders() {
        Long userId = requireUser().userId();
        return Result.success(folderService.listByUser(userId));
    }

    @PostMapping("/sort")
    @Operation(summary = "更新文件夹顺序", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> sortFolders(@Valid @RequestBody SortReq req) {
        Long userId = requireUser().userId();
        return Result.success(folderService.updateOrders(userId, req.getFolderIdsInOrder()));
    }

    @Data
    @Schema(name = "FolderCreateReq", description = "创建文件夹请求")
    public static class CreateReq {
        @NotBlank
        @Schema(description = "文件夹名称", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private String name;
        @Schema(description = "排序值，可选")
        private Integer sort;
    }

    @Data
    @Schema(name = "FolderRenameReq", description = "重命名文件夹请求")
    public static class RenameReq {
        @NotNull
        @Schema(description = "文件夹ID", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private Long folderId;
        @NotBlank
        @Schema(description = "新名称", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private String name;
    }

    @Data
    @Schema(name = "FolderDeleteReq", description = "删除文件夹请求")
    public static class DeleteReq {
        @NotNull
        @Schema(description = "文件夹ID", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private Long folderId;
    }

    @Data
    @Schema(name = "FolderSortReq", description = "更新文件夹顺序请求")
    public static class SortReq {
        @NotNull
        @Schema(description = "按顺序排列的文件夹ID列表", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        private List<Long> folderIdsInOrder;
    }
}
