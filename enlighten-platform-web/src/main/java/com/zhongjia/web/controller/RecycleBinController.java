package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.RecycleBinItem;
import com.zhongjia.biz.service.RecycleBinService;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.PageResponse;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "回收站管理")
@RequestMapping("/api/recycle")
public class RecycleBinController {

    @Autowired
    private RecycleBinService recycleBinService;

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询回收站", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<PageResponse<RecycleBinItem>> pageRecycle(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = requireUser().userId();
        Page<RecycleBinItem> p = recycleBinService.page(userId, page, size);
        return Result.success(PageResponse.of(page, size, p.getTotal(), p.getRecords()));
    }

    @PostMapping("/restore")
    @Operation(summary = "从回收站恢复文章", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> restore(@RequestBody List<Long> recycleIds) {
        Long userId = requireUser().userId();
        return Result.success(recycleBinService.restoreArticles(userId, recycleIds));
    }

    @PostMapping("/empty")
    @Operation(summary = "清空回收站（删除选中项）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> empty(@RequestBody List<Long> recycleIds) {
        Long userId = requireUser().userId();
        return Result.success(recycleBinService.empty(userId, recycleIds));
    }
}
