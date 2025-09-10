package com.zhongjia.web.controller;

import com.zhongjia.biz.service.DhModelService;
import com.zhongjia.biz.service.dto.UpstreamResult;
import com.zhongjia.web.exception.BizException;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "数字人模型")
@RequestMapping("/api/dh")
public class DigitalHumanModelController {

    @Autowired
    private DhModelService dhModelService;

    @GetMapping("/models")
    @Operation(summary = "获取数字人模型详细列表（来源于上游三方接口）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<DhModelDetailListResponse> listModels() {
        UserContext.UserInfo user = requireUser();
        List<java.util.Map<String, Object>> items = dhModelService.listModelDetailsForUser(user.userId());
        DhModelDetailListResponse resp = new DhModelDetailListResponse();
        resp.setModels(items);
        return Result.success(resp);
    }

    @PostMapping(value = "/train", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传视频训练数字人（multipart/form-data: file + model_name）", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Map<String, Object>> trainMultipart(
            @RequestPart("file") MultipartFile file,
            @RequestPart("model_name") String modelName
    ) {
        UserContext.UserInfo user = requireUser();
        UpstreamResult upstream = dhModelService.trainWithFile(user.userId(), modelName, file);
        return Result.success(Map.of(
                "code", upstream.getCode(),
                "success", upstream.getSuccess(),
                "message", upstream.getMsg(),
                "data", upstream.getDataRaw()
        ));
    }


    private UserContext.UserInfo requireUser() {
        UserContext.UserInfo info = UserContext.get();
        if (info == null || info.userId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return info;
    }

    @Data
    public static class DhModelDetailListResponse {
        private List<java.util.Map<String, Object>> models;
    }
}


