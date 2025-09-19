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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<Map<String, Long>> createFolder(@RequestParam String name, @RequestParam(required = false) Integer sort) {
        Long userId = requireUser().userId();
        Long id = folderService.create(userId, name, sort);
        return id == null ? Result.error(ErrorCode.INTERNAL_ERROR, "创建失败") : Result.success(Map.of("id", id));
    }

    @PostMapping("/rename")
    @Operation(summary = "重命名文件夹", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> renameFolder(@RequestParam Long folderId, @RequestParam String name) {
        Long userId = requireUser().userId();
        return Result.success(folderService.rename(userId, folderId, name));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除文件夹（软删除，文件夹与文章进入回收站）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> deleteFolder(@RequestParam Long folderId) {
        Long userId = requireUser().userId();
        return Result.success(folderService.deleteSoft(userId, folderId));
    }

    @GetMapping("/list")
    @Operation(summary = "按顺序查询文件夹列表", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<List<Folder>> listFolders() {
        Long userId = requireUser().userId();
        return Result.success(folderService.listByUser(userId));
    }

    @PostMapping("/sort")
    @Operation(summary = "更新文件夹顺序", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> sortFolders(@RequestBody List<Long> folderIdsInOrder) {
        Long userId = requireUser().userId();
        return Result.success(folderService.updateOrders(userId, folderIdsInOrder));
    }
}
