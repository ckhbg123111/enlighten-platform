package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.DraftPO;
import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.service.DraftService;
import com.zhongjia.biz.repository.MediaConvertRecordRepository;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.DraftVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "草稿管理")
@RequestMapping("/api/drafts")
public class DraftController {

	@Autowired
	private DraftService draftService;

	@Autowired
	private MediaConvertRecordRepository mediaConvertRecordRepository;

    @PostMapping("/save")
    @Operation(summary = "保存或更新草稿", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Long> save(@Valid @RequestBody SaveReq req) {
        UserContext.UserInfo user = requireUser();
        // 增加mediaCode有效性校验，检查media_convert_record表中是否存在
        if (req.getMediaCodeList() != null && !req.getMediaCodeList().isEmpty()) {
            java.util.Set<String> codes = new java.util.HashSet<>(req.getMediaCodeList());
            LambdaQueryWrapper<MediaConvertRecord> w =
                    new LambdaQueryWrapper<MediaConvertRecord>()
                            .eq(MediaConvertRecord::getUserId, user.userId())
                            .in(MediaConvertRecord::getCode, codes)
                            .eq(MediaConvertRecord::getDeleted, 0);
            long count = mediaConvertRecordRepository.count(w);
            if (count < codes.size()) {
                return Result.error(ErrorCode.BAD_REQUEST.getCode(), "存在无效的mediaCode");
            }
        }
        Long id = draftService.saveOrUpdateByEssayCode(
            user.userId(),
            user.tenantId(),
            req.getEssayCode(),
            req.getTitle(),
            req.getContent(),
            req.getMediaCodeList(),
            req.getTag()
        );
        return Result.success(id);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询草稿", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Page<DraftVO>> list(@Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
                                      @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int pageSize) {
        UserContext.UserInfo user = requireUser();
        Page<DraftPO> result = draftService.pageByUser(user.userId(), page, pageSize);
        Page<DraftVO> voPage = new Page<>();
        org.springframework.beans.BeanUtils.copyProperties(result, voPage);
        java.util.List<DraftVO> voList = new java.util.ArrayList<>();
        for (DraftPO d : result.getRecords()) {
            DraftVO vo = new DraftVO();
            vo.setId(d.getId());
            vo.setEssayCode(d.getEssayCode());
            vo.setTitle(d.getTitle());
            vo.setContent(d.getContent());
            vo.setTags(d.getTags());
            vo.setDeleted(d.getDeleted());
			if (d.getMediaCodeListString() != null) {
				// 将字符串转换为列表
				List<String> mediaIds = List.of(d.getMediaCodeListString().split(","));
				vo.setMediaCodeList(mediaIds);
			} else {
				vo.setMediaCodeList(null);
			}
            vo.setCreateTime(d.getCreateTime());
            vo.setUpdateTime(d.getUpdateTime());
            vo.setDeleteTime(d.getDeleteTime());
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return Result.success(voPage);
	}

    @PostMapping("/edit")
    @Operation(summary = "编辑草稿", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> edit(@Valid @RequestBody EditReq req) {
        UserContext.UserInfo user = requireUser();
        boolean ok = draftService.editDraft(user.userId(), req.getDraftId(), req.getTitle(), req.getContent());
        if (!ok) return Result.error(404, "草稿不存在");
        return Result.success(true);
	}

    @PostMapping("/delete")
    @Operation(summary = "删除草稿(软删除)", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> delete(@Valid @RequestBody DeleteReq req) {
        UserContext.UserInfo user = requireUser();
        boolean ok = draftService.softDelete(user.userId(), req.getDraftId());
        if (!ok) return Result.error(404, "草稿不存在");
        return Result.success(true);
	}

    @PostMapping("/restore")
    @Operation(summary = "恢复草稿", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> restore(@Valid @RequestBody RestoreReq req) {
        UserContext.UserInfo user = requireUser();
        boolean ok = draftService.restore(user.userId(), req.getDraftId());
        if (!ok) return Result.error(404, "草稿不存在");
        return Result.success(true);
	}

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

    @Data
    @Schema(name = "DraftSaveReq", description = "保存草稿请求")
    public static class SaveReq {
        @Schema(description = "标题", example = "量子力学入门")
        @NotBlank
        private String title;
        @Schema(description = "正文内容")
        @NotBlank
        private String content;
        @Schema(description = "文章唯一编码")
        @NotBlank
        private String essayCode;
        @ArraySchema(schema = @Schema(description = "媒体素材编码列表", implementation = String.class))
        private List<String> mediaCodeList;
        @Schema(description = "标签", example = "科普,AI")
        private String tag;

    }

    @Data
    @Schema(name = "DraftEditReq", description = "编辑草稿请求")
    public static class EditReq {
        @Schema(description = "草稿ID")
        @NotNull
        private Long draftId;
        @Schema(description = "标题")
        private String title;
        @Schema(description = "正文内容")
        private String content;
    }

    @Data
    @Schema(name = "DraftDeleteReq", description = "删除草稿请求")
    public static class DeleteReq {
        @Schema(description = "草稿ID")
        @NotNull
        private Long draftId;
    }

    @Data
    @Schema(name = "DraftRestoreReq", description = "恢复草稿请求")
    public static class RestoreReq {
        @Schema(description = "草稿ID")
        @NotNull
        private Long draftId;
    }
}



