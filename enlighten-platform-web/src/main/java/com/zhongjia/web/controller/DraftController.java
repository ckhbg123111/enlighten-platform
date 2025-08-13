package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.DraftPO;
import com.zhongjia.biz.service.DraftService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.DraftVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drafts")
public class DraftController {

	@Autowired
	private DraftService draftService;

    @PostMapping("/save")
    public Result<Long> save(@Valid @RequestBody SaveReq req) {
        UserContext.UserInfo user = requireUser();
        Long id = draftService.saveOrUpdateByEssayCode(user.userId(), user.tenantId(), req.getEssayCode(), req.getTitle(), req.getContent(), req.getMediaCodeList());
        return Result.success(id);
    }

	@GetMapping("/list")
    public Result<Page<DraftVO>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
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
            vo.setDeleted(d.getDeleted());
			if (d.getMediaIdListString() != null) {
				// 将字符串转换为列表
				List<String> mediaIds = List.of(d.getMediaIdListString().split(","));
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
	public Result<Boolean> edit(@Valid @RequestBody EditReq req) {
        UserContext.UserInfo user = requireUser();
        boolean ok = draftService.editDraft(user.userId(), req.getDraftId(), req.getTitle(), req.getContent());
        if (!ok) return Result.error(404, "草稿不存在");
        return Result.success(true);
	}

	@PostMapping("/delete")
	public Result<Boolean> delete(@Valid @RequestBody DeleteReq req) {
        UserContext.UserInfo user = requireUser();
        boolean ok = draftService.softDelete(user.userId(), req.getDraftId());
        if (!ok) return Result.error(404, "草稿不存在");
        return Result.success(true);
	}

	@PostMapping("/restore")
	public Result<Boolean> restore(@Valid @RequestBody RestoreReq req) {
        UserContext.UserInfo user = requireUser();
        boolean ok = draftService.restore(user.userId(), req.getDraftId());
        if (!ok) return Result.error(404, "草稿不存在");
        return Result.success(true);
	}

	private UserContext.UserInfo requireUser() {
		UserContext.UserInfo info = UserContext.get();
		if (info == null || info.userId() == null) throw new RuntimeException("未认证");
		return info;
	}

	@Data
	public static class SaveReq {
		@NotBlank
		private String title;
		@NotBlank
		private String content;
		@NotBlank
		private String essayCode;
		private List<String> mediaCodeList;
	}

	@Data
	public static class EditReq {
		@NotNull
		private Long draftId;
		private String title;
		private String content;
	}

	@Data
	public static class DeleteReq {
		@NotNull
		private Long draftId;
	}

	@Data
	public static class RestoreReq {
		@NotNull
		private Long draftId;
	}
}



