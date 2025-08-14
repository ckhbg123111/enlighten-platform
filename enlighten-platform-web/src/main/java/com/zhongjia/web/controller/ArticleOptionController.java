package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.UserArticleConfig;
import com.zhongjia.biz.service.UserArticleConfigService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.ArticleOptionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Tag(name = "文章选项")
@RequestMapping("/api/article/options")
public class ArticleOptionController {

    private static final java.util.List<String> CATEGORIES = java.util.List.of("style", "length", "mode", "scene");

    @Autowired
    private UserArticleConfigService configService;


    // 接口一：选项列表，GET，无参，通过 jwt 获取用户ID
    @GetMapping
    @Operation(summary = "获取用户文章选项列表", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Map<String, List<ArticleOptionVO>>> listAll() {
        Long userId = requireUserId();
        Map<String, List<UserArticleConfig>> grouped = configService.loadOrInitByUser(userId);
        Map<String, List<ArticleOptionVO>> resp = new LinkedHashMap<>();
        for (String cat : CATEGORIES) resp.put(cat, new ArrayList<>());
        grouped.forEach((cat, list) -> {
            List<ArticleOptionVO> vos = new ArrayList<>();
            for (UserArticleConfig c : list) {
                ArticleOptionVO vo = new ArticleOptionVO();
                vo.setId(c.getId());
                vo.setCategory(c.getCategory());
                vo.setOptionName(c.getOptionName());
                vo.setOptionCode(c.getOptionCode());
                vo.setSort(c.getSort());
                vo.setCreateTime(c.getCreateTime());
                vo.setUpdateTime(c.getUpdateTime());
                vos.add(vo);
            }
            resp.put(cat, vos);
        });
        return Result.success(resp);
    }

    // 接口二：选项更新，POST，按 option_code 修改名称/排序
    @PostMapping("/update")
    @Operation(summary = "更新选项", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> update(@Valid @RequestBody UpdateReq req) {
        Long userId = requireUserId();
        UserArticleConfig exist = configService.getByUserIdAndOptionCode(userId, req.getOptionCode());
        if (exist == null) return Result.error(404, "选项不存在");
        if (req.getOptionName() != null) exist.setOptionName(req.getOptionName());
        if (req.getSort() != null) exist.setSort(req.getSort());
        return Result.success(configService.updateById(exist));
    }

    // 接口三：选项新增，POST，为大类新增选项
    @PostMapping("/add")
    @Operation(summary = "新增选项", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<ArticleOptionVO> add(@Valid @RequestBody AddReq req) {
        Long userId = requireUserId();
        if (!CATEGORIES.contains(req.getCategory())) {
            return Result.error(400, "非法分类");
        }
        UserArticleConfig cfg = new UserArticleConfig();
        BeanUtils.copyProperties(req, cfg);
        cfg.setUserId(userId);
        cfg.setOptionCode(UUID.randomUUID().toString());
        cfg.setSort(Optional.ofNullable(req.getSort()).orElse(0));
        configService.save(cfg);
        ArticleOptionVO vo = new ArticleOptionVO();
        vo.setId(cfg.getId());
        vo.setCategory(cfg.getCategory());
        vo.setOptionName(cfg.getOptionName());
        vo.setOptionCode(cfg.getOptionCode());
        vo.setSort(cfg.getSort());
        vo.setCreateTime(cfg.getCreateTime());
        vo.setUpdateTime(cfg.getUpdateTime());
        return Result.success(vo);
    }

    // 接口四：选项删除，DELETE，通过选项 code 删除
    @DeleteMapping("/{optionCode}")
    @Operation(summary = "删除选项", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Boolean> delete(@Parameter(description = "选项编码") @PathVariable String optionCode) {
        Long userId = requireUserId();
        boolean ok = configService.removeByUserIdAndOptionCode(userId, optionCode);
        return ok ? Result.success(true) : Result.error(404, "选项不存在");
    }

    private Long requireUserId() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        return info.userId();
    }

    // 默认选项初始化逻辑已移动至 Service 层

    @Data
    @Schema(name = "ArticleOptionUpdateReq", description = "更新选项请求")
    public static class UpdateReq {
        @Schema(description = "选项编码")
        @NotBlank
        private String optionCode;
        @Schema(description = "选项名称")
        private String optionName;
        @Schema(description = "排序值，越大越靠后")
        private Integer sort;
    }

    @Data
    @Schema(name = "ArticleOptionAddReq", description = "新增选项请求")
    public static class AddReq {
        @Schema(description = "分类，可选：style/length/mode/scene")
        @NotBlank
        private String category;
        @Schema(description = "选项名称")
        @NotBlank
        private String optionName;
        @Schema(description = "排序值，越大越靠后")
        private Integer sort;
    }
}


