package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjia.biz.entity.UserArticleConfig;
import com.zhongjia.biz.service.UserArticleConfigService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/article/options")
public class ArticleOptionController {

    private static final java.util.List<String> CATEGORIES = java.util.List.of("style", "length", "mode", "scene");

    @Autowired
    private UserArticleConfigService configService;

    // 接口一：选项列表，GET，无参，通过 jwt 获取用户ID
    @GetMapping
    public Result<Map<String, List<UserArticleConfig>>> listAll() {
        Long userId = requireUserId();
        List<UserArticleConfig> list = configService.listByUserId(userId);
        if (list.isEmpty()) {
            // 首次访问：注入默认选项
            List<UserArticleConfig> seeds = buildDefaultOptions(userId);
            if (!seeds.isEmpty()) {
                configService.saveBatch(seeds);
                list = configService.listByUserId(userId);
            }
        }
        Map<String, List<UserArticleConfig>> grouped = new LinkedHashMap<>();
        for (String cat : CATEGORIES) grouped.put(cat, new ArrayList<>());
        for (UserArticleConfig c : list) {
            grouped.computeIfAbsent(c.getCategory(), k -> new ArrayList<>()).add(c);
        }
        return Result.success(grouped);
    }

    // 接口二：选项更新，POST，按 option_code 修改名称/排序
    @PostMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody UpdateReq req) {
        Long userId = requireUserId();
        UserArticleConfig exist = configService.getOne(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .eq(UserArticleConfig::getOptionCode, req.getOptionCode()));
        if (exist == null) return Result.error(404, "选项不存在");
        if (req.getOptionName() != null) exist.setOptionName(req.getOptionName());
        if (req.getSort() != null) exist.setSort(req.getSort());
        return Result.success(configService.updateById(exist));
    }

    // 接口三：选项新增，POST，为大类新增选项
    @PostMapping("/add")
    public Result<UserArticleConfig> add(@Valid @RequestBody AddReq req) {
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
        return Result.success(cfg);
    }

    // 接口四：选项删除，DELETE，通过选项 code 删除
    @DeleteMapping("/{optionCode}")
    public Result<Boolean> delete(@PathVariable String optionCode) {
        Long userId = requireUserId();
        boolean ok = configService.removeByUserIdAndOptionCode(userId, optionCode);
        return ok ? Result.success(true) : Result.error(404, "选项不存在");
    }

    private Long requireUserId() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) throw new RuntimeException("未认证");
        return info.userId();
    }

    private List<UserArticleConfig> buildDefaultOptions(Long userId) {
        Map<String, List<String>> defaults = new LinkedHashMap<>();
        defaults.put("style", List.of("亲切口语", "专业严谨", "故事化", "小红书", "公众号"));
        defaults.put("length", List.of("短", "中", "长"));
        defaults.put("mode", List.of("常规文章", "双人对话", "故事案例"));
        defaults.put("scene", List.of("日常科普", "入院须知", "术前指导", "术后康复", "慢病管理"));

        List<UserArticleConfig> list = new ArrayList<>();
        defaults.forEach((category, names) -> {
            for (int i = 0; i < names.size(); i++) {
                UserArticleConfig c = new UserArticleConfig();
                c.setUserId(userId);
                c.setCategory(category);
                c.setOptionName(names.get(i));
                c.setOptionCode(UUID.randomUUID().toString());
                c.setSort(i);
                list.add(c);
            }
        });
        return list;
    }

    @Data
    public static class UpdateReq {
        @NotBlank
        private String optionCode;
        private String optionName;
        private Integer sort;
    }

    @Data
    public static class AddReq {
        @NotBlank
        private String category;
        @NotBlank
        private String optionName;
        private Integer sort;
    }
}


