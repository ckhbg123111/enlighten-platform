package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/drafts")
public class DraftController {

	@Autowired
	private DraftService draftService;

	@PostMapping("/save")
	public Result<Long> save(@Valid @RequestBody SaveReq req) {
		UserContext.UserInfo user = requireUser();
		DraftPO exist = draftService.getOne(new LambdaQueryWrapper<DraftPO>()
				.eq(DraftPO::getUserId, user.userId())
				.eq(DraftPO::getEssayCode, req.getEssayCode())
				.last("limit 1"));
		if (exist == null) {
			DraftPO draftPO = new DraftPO()
					.setUserId(user.userId())
					.setTenantId(user.tenantId())
					.setEssayCode(req.getEssayCode())
					.setTitle(req.getTitle())
					.setContent(req.getContent())
					.setDeleted(0)
					.setCreateTime(LocalDateTime.now())
					.setUpdateTime(LocalDateTime.now());
			draftService.save(draftPO);
			return Result.success(draftPO.getId());
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
    public Result<Page<DraftVO>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
		UserContext.UserInfo user = requireUser();
		Page<DraftPO> p = new Page<>(page, pageSize);
		LambdaQueryWrapper<DraftPO> w = new LambdaQueryWrapper<DraftPO>()
				.eq(DraftPO::getUserId, user.userId())
				.eq(DraftPO::getDeleted, 0)
				.orderByDesc(DraftPO::getUpdateTime);
        Page<DraftPO> result = draftService.page(p, w);
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
		DraftPO draftPO = draftService.getById(req.getDraftId());
		if (draftPO == null || !draftPO.getUserId().equals(user.userId())) {
			return Result.error(404, "草稿不存在");
		}
		if (req.getTitle() != null) draftPO.setTitle(req.getTitle());
		if (req.getContent() != null) draftPO.setContent(req.getContent());
		draftPO.setUpdateTime(LocalDateTime.now());
		return Result.success(draftService.updateById(draftPO));
	}

	@PostMapping("/delete")
	public Result<Boolean> delete(@Valid @RequestBody DeleteReq req) {
		UserContext.UserInfo user = requireUser();
		DraftPO draftPO = draftService.getById(req.getDraftId());
		if (draftPO == null || !draftPO.getUserId().equals(user.userId())) {
			return Result.error(404, "草稿不存在");
		}
		draftPO.setDeleted(1);
		draftPO.setDeleteTime(LocalDateTime.now());
		draftPO.setUpdateTime(LocalDateTime.now());
		return Result.success(draftService.updateById(draftPO));
	}

	@PostMapping("/restore")
	public Result<Boolean> restore(@Valid @RequestBody RestoreReq req) {
		UserContext.UserInfo user = requireUser();
		DraftPO draftPO = draftService.getById(req.getDraftId());
		if (draftPO == null || !draftPO.getUserId().equals(user.userId())) {
			return Result.error(404, "草稿不存在");
		}
		draftPO.setDeleted(0);
		draftPO.setDeleteTime(null);
		draftPO.setUpdateTime(LocalDateTime.now());
		return Result.success(draftService.updateById(draftPO));
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



