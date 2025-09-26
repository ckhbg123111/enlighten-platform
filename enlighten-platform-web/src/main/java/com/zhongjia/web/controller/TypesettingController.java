package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.TypesettingTemplate;
import com.zhongjia.biz.entity.TypesettingMaterial;
import com.zhongjia.biz.entity.User;
import com.zhongjia.biz.service.TypesettingTemplateService;
import com.zhongjia.biz.service.TypesettingMaterialService;
import com.zhongjia.biz.service.UserService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.TypesettingTemplateInfoVO;
import com.zhongjia.web.vo.TypesettingMaterialVO;
import com.zhongjia.web.vo.TypesettingTemplateDetailVO;
import com.zhongjia.web.vo.PageResponse;
import com.zhongjia.web.mapper.TypesettingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 排版控制器
 */
@RestController
@Tag(name = "排版管理")
@RequestMapping("/api/typesetting")
public class TypesettingController {

    @Autowired
    private TypesettingTemplateService templateService;

    @Autowired
    private TypesettingMaterialService materialService;

    @Autowired
    private TypesettingMapper typesettingMapper;

    @Autowired
    private UserService userService;

    /**
     * 分页查询模板列表
     */
    @GetMapping("/templates")
    @Operation(summary = "分页查询模板列表", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<PageResponse<TypesettingTemplateInfoVO>> getTemplates(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        UserContext.UserInfo userInfo = requireUser();
        User user = userService.getById(userInfo.userId());
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        
        // 按照用户所在医院和科室过滤
        Page<TypesettingTemplate> templatePage = templateService.getTemplatesByHospitalAndDepartment(
                user.getHospital(), user.getDepartment(), page, size);
        
        // 转换为VO
        List<TypesettingTemplateInfoVO> voList = templatePage.getRecords().stream()
                .map(typesettingMapper::toTemplateVO)
                .collect(Collectors.toList());
        
        PageResponse<TypesettingTemplateInfoVO> resp = PageResponse.of(page, size, templatePage.getTotal(), voList);
        return Result.success(resp);
    }

    /**
     * 分页查询素材列表
     */
    @GetMapping("/materials")
    @Operation(summary = "分页查询素材列表", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<PageResponse<TypesettingMaterialVO>> getMaterials(
            @Parameter(description = "素材类型") @RequestParam String type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        UserContext.UserInfo userInfo = requireUser();
        User user = userService.getById(userInfo.userId());
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        
        // 按照用户所在医院和科室过滤
        Page<TypesettingMaterial> materialPage = materialService.getMaterialsByTypeAndHospitalAndDepartment(
                type, user.getHospital(), user.getDepartment(), page, size);
        
        // 转换为VO
        List<TypesettingMaterialVO> voList = materialPage.getRecords().stream()
                .map(typesettingMapper::toMaterialVO)
                .collect(Collectors.toList());
        
        PageResponse<TypesettingMaterialVO> resp = PageResponse.of(page, size, materialPage.getTotal(), voList);
        return Result.success(resp);
    }

    /**
     * 按ID查询模板详情
     */
    @GetMapping("/templates/{id}")
    @Operation(summary = "按ID查询模板详情", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<TypesettingTemplateDetailVO> getTemplateDetail(
            @Parameter(description = "模板ID") @PathVariable("id") Long id) {

        UserContext.UserInfo userInfo = requireUser();
        User user = userService.getById(userInfo.userId());
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }

        TypesettingTemplate template = templateService.getById(id);
        if (template == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }

        // 权限：仅允许访问同医院（与分页列表保持一致）。科室暂未限制。
        if (template.getHospital() != null && user.getHospital() != null && !template.getHospital().equals(user.getHospital())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        TypesettingTemplateDetailVO vo = typesettingMapper.toTemplateDetailVO(template);
        return Result.success(vo);
    }

    /**
     * 获取当前用户信息并验证
     */
    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return info;
    }
}
