package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.Draft;
import com.zhongjia.biz.service.DraftService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/drafts")
public class DraftController {

	@Autowired
	private DraftService draftService;

	@PostMapping("/save")
	public Result<Long> save(@Valid @RequestBody SaveReq req) {
		UserContext.UserInfo user = requireUser();
		Draft exist = draftService.getOne(new LambdaQueryWrapper<Draft>()
				.eq(Draft::getUserId, user.userId())
				.eq(Draft::getEssayCode, req.getEssayCode())
				.last("limit 1"));
		if (exist == null) {
			Draft draft = new Draft()
					.setUserId(user.userId())
					.setTenantId(user.tenantId())
					.setEssayCode(req.getEssayCode())
					.setTitle(req.getTitle())
					.setContent(req.getContent())
					.setDeleted(0)
					.setCreateTime(LocalDateTime.now())
					.setUpdateTime(LocalDateTime.now());
			draftService.save(draft);
			return Result.success(draft.getId());
		} else {
			exist.setTitle(req.getTitle());
			exist.setContent(req.getContent());
			exist.setDeleted(0);
			exist.setUpdateTime(LocalDateTime.now());
			draftService.updateById(exist);
			return Result.success(exist.getId());
		}
	}

	@GetMapping("/list")
	public Result<Page<Draft>> list(@RequestParam(defaultValue = "1") int page,
									@RequestParam(defaultValue = "10") int pageSize) {
		UserContext.UserInfo user = requireUser();
		Page<Draft> p = new Page<>(page, pageSize);
		LambdaQueryWrapper<Draft> w = new LambdaQueryWrapper<Draft>()
				.eq(Draft::getUserId, user.userId())
				.eq(Draft::getDeleted, 0)
				.orderByDesc(Draft::getUpdateTime);
		Page<Draft> result = draftService.page(p, w);
		return Result.success(result);
	}

	@PostMapping("/edit")
	public Result<Boolean> edit(@Valid @RequestBody EditReq req) {
		UserContext.UserInfo user = requireUser();
		Draft draft = draftService.getById(req.getDraftId());
		if (draft == null || !draft.getUserId().equals(user.userId())) {
			return Result.error(404, "草稿不存在");
		}
		if (req.getTitle() != null) draft.setTitle(req.getTitle());
		if (req.getContent() != null) draft.setContent(req.getContent());
		draft.setUpdateTime(LocalDateTime.now());
		return Result.success(draftService.updateById(draft));
	}

	@PostMapping("/delete")
	public Result<Boolean> delete(@Valid @RequestBody DeleteReq req) {
		UserContext.UserInfo user = requireUser();
		Draft draft = draftService.getById(req.getDraftId());
		if (draft == null || !draft.getUserId().equals(user.userId())) {
			return Result.error(404, "草稿不存在");
		}
		draft.setDeleted(1);
		draft.setDeleteTime(LocalDateTime.now());
		draft.setUpdateTime(LocalDateTime.now());
		return Result.success(draftService.updateById(draft));
	}

	@PostMapping("/restore")
	public Result<Boolean> restore(@Valid @RequestBody RestoreReq req) {
		UserContext.UserInfo user = requireUser();
		Draft draft = draftService.getById(req.getDraftId());
		if (draft == null || !draft.getUserId().equals(user.userId())) {
			return Result.error(404, "草稿不存在");
		}
		draft.setDeleted(0);
		draft.setDeleteTime(null);
		draft.setUpdateTime(LocalDateTime.now());
		return Result.success(draftService.updateById(draft));
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



