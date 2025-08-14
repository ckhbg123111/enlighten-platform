package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjia.biz.entity.DraftMediaMap;
import com.zhongjia.biz.entity.MediaConvertRecord;
import com.zhongjia.biz.repository.DraftMediaMapRepository;
import com.zhongjia.biz.repository.MediaConvertRecordRepository;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@Tag(name = "内容库")
@RequestMapping("/api/content-library")
public class ContentLibraryController {

    @Autowired
    private DraftMediaMapRepository draftMediaMapRepository;

    @Autowired
    private MediaConvertRecordRepository mediaConvertRecordRepository;

    @GetMapping("/query")
    @Operation(summary = "按用户+标签查询内容库", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<List<ItemVO>> query(
            @Parameter(description = "标签", example = "科普") @RequestParam(required = false) String tag) {
        UserContext.UserInfo user = requireUser();

        LambdaQueryWrapper<DraftMediaMap> w = new LambdaQueryWrapper<DraftMediaMap>()
                .eq(DraftMediaMap::getUserId, user.userId())
                .eq(DraftMediaMap::getDeleted, 0);
        if (tag != null && !tag.isEmpty()) {
            w.eq(DraftMediaMap::getTag, tag);
        }
        List<DraftMediaMap> maps = draftMediaMapRepository.list(w);
        if (maps.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<String> mediaCodes = new HashSet<>();
        for (DraftMediaMap m : maps) mediaCodes.add(m.getMediaCode());

        List<MediaConvertRecord> records = mediaConvertRecordRepository.list(new LambdaQueryWrapper<MediaConvertRecord>()
                .eq(MediaConvertRecord::getUserId, user.userId())
                .in(MediaConvertRecord::getCode, mediaCodes)
                .eq(MediaConvertRecord::getDeleted, 0));

        Map<String, MediaConvertRecord> code2rec = new HashMap<>();
        for (MediaConvertRecord r : records) code2rec.put(r.getCode(), r);

        List<ItemVO> result = new ArrayList<>();
        for (DraftMediaMap m : maps) {
            MediaConvertRecord r = code2rec.get(m.getMediaCode());
            if (r == null) continue;
            ItemVO vo = new ItemVO();
            vo.setDraftId(m.getDraftId());
            vo.setTag(m.getTag());
            vo.setMediaCode(r.getCode());
            vo.setEssayCode(r.getEssayCode());
            vo.setPlatform(r.getPlatform());
            vo.setContent(r.getContent());
            result.add(vo);
        }
        return Result.success(result);
    }

    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info;
    }

    @Data
    @Schema(name = "ContentLibraryItem", description = "内容库条目")
    public static class ItemVO {
        @Schema(description = "草稿ID")
        private Long draftId;
        @Schema(description = "标签")
        private String tag;
        @Schema(description = "媒体编码")
        private String mediaCode;
        @Schema(description = "文章编码")
        private String essayCode;
        @Schema(description = "平台")
        private String platform;
        @Schema(description = "内容")
        private String content;
    }
}



