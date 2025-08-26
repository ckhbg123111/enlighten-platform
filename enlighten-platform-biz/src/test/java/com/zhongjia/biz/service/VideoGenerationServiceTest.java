package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import com.zhongjia.biz.service.impl.VideoGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 视频生成服务测试
 */
@ExtendWith(MockitoExtension.class)
public class VideoGenerationServiceTest {
    
    @Mock
    private VideoGenerationTaskRepository taskRepository;
    
    @InjectMocks
    private VideoGenerationServiceImpl videoGenerationService;
    
    @Test
    public void testCreateTask() {
        // 准备测试数据
        Long userId = 1L;
        Long tenantId = 1L;
        String inputText = "这是一个测试文本";
        String modelName = "test_model";
        String voice = "Female_Voice_1";
        
        // Mock 仓库保存操作
        when(taskRepository.save(any(VideoGenerationTask.class))).thenAnswer(invocation -> {
            VideoGenerationTask task = invocation.getArgument(0);
            task.setId("test-task-id");
            return task;
        });
        
        // 执行测试
        String taskId = videoGenerationService.createTask(userId, tenantId, inputText, modelName, voice);
        
        // 验证结果
        assertEquals("test-task-id", taskId);
        verify(taskRepository, times(1)).save(any(VideoGenerationTask.class));
    }
    
    @Test
    public void testGetTaskStatus() {
        // 准备测试数据
        String taskId = "test-task-id";
        Long userId = 1L;
        Long tenantId = 1L;
        
        VideoGenerationTask task = new VideoGenerationTask()
                .setId(taskId)
                .setUserId(userId)
                .setTenantId(tenantId)
                .setStatus("DH_PROCESSING")
                .setProgress(30);
        
        // Mock 仓库查询操作
        when(taskRepository.getById(taskId)).thenReturn(task);
        
        // 执行测试
        VideoGenerationTask result = videoGenerationService.getTaskStatus(taskId, userId, tenantId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("DH_PROCESSING", result.getStatus());
        assertEquals(30, result.getProgress());
    }
    
    @Test
    public void testGetTaskStatusUnauthorized() {
        // 准备测试数据
        String taskId = "test-task-id";
        Long userId = 1L;
        Long tenantId = 1L;
        Long wrongUserId = 2L;
        
        VideoGenerationTask task = new VideoGenerationTask()
                .setId(taskId)
                .setUserId(userId)
                .setTenantId(tenantId);
        
        // Mock 仓库查询操作
        when(taskRepository.getById(taskId)).thenReturn(task);
        
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            videoGenerationService.getTaskStatus(taskId, wrongUserId, tenantId);
        });
    }
    
    @Test
    public void testUpdateTaskStatus() {
        // 准备测试数据
        VideoGenerationTask task = new VideoGenerationTask()
                .setId("test-task-id")
                .setStatus("DH_PROCESSING")
                .setProgress(30);
        
        // Mock 仓库更新操作
        when(taskRepository.updateById(any(VideoGenerationTask.class))).thenReturn(true);
        
        // 执行测试
        videoGenerationService.updateTaskStatus(task, "DH_DONE", 60, null);
        
        // 验证结果
        assertEquals("DH_DONE", task.getStatus());
        assertEquals(60, task.getProgress());
        assertNull(task.getErrorMessage());
        verify(taskRepository, times(1)).updateById(task);
    }
}
