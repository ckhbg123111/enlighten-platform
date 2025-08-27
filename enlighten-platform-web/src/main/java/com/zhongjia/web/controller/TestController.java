package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import com.zhongjia.biz.service.VideoGenerationService;
import com.zhongjia.web.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "测试接口")
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private VideoGenerationTaskRepository taskRepository;

    @Autowired
    private VideoGenerationService videoGenerationService;

    @GetMapping("/process-burn-first")
    @Operation(summary = "触发字幕烧录：取数据库中第一条记录并调用processBurnPhase")
    public Result<String> processBurnFirst() {
        List<VideoGenerationTask> list = taskRepository.list(
                new QueryWrapper<VideoGenerationTask>()
                        .orderByAsc("created_at")
                        .last("limit 1")
        );
        if (list == null || list.isEmpty()) {
            return Result.error(404, "未找到任何视频任务记录");
        }

        VideoGenerationTask task = list.get(0);
        try {
            videoGenerationService.processBurnPhase(task);
            return Result.success("已触发字幕烧录，任务ID=" + task.getId());
        } catch (Exception e) {
            return Result.error(500, "触发失败: " + e.getMessage());
        }
    }
}


