package com.zhongjia.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.User;
import com.zhongjia.biz.service.UserService;
import com.zhongjia.web.req.UserCreateReq;
import com.zhongjia.web.req.UserUpdateReq;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建用户
     */
    @PostMapping("/create")
    public Result<UserVO> createUser(@Validated @RequestBody UserCreateReq req) {
        // 检查用户名是否已存在
        User existingUser = userService.getByUsername(req.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(req, user);
        boolean success = userService.save(user);
        
        if (success) {
            UserVO userVO = convertToVO(user);
            return Result.success(userVO);
        } else {
            return Result.error("创建用户失败");
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<UserVO> updateUser(@Validated @RequestBody UserUpdateReq req) {
        User user = userService.getById(req.getId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(req, user);
        boolean success = userService.updateById(user);
        
        if (success) {
            UserVO userVO = convertToVO(user);
            return Result.success(userVO);
        } else {
            return Result.error("更新用户失败");
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除用户失败");
        }
    }
    
    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserVO userVO = convertToVO(user);
        return Result.success(userVO);
    }
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<Page<UserVO>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> userPage = userService.page(page, wrapper);
        
        // 转换为VO
        Page<UserVO> userVOPage = new Page<>();
        BeanUtils.copyProperties(userPage, userVOPage);
        
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        userVOPage.setRecords(userVOList);
        
        return Result.success(userVOPage);
    }
    
    /**
     * 查询所有用户
     */
    @GetMapping("/list")
    public Result<List<UserVO>> getAllUsers() {
        List<User> users = userService.list();
        List<UserVO> userVOList = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return Result.success(userVOList);
    }
    
    /**
     * 根据状态查询用户
     */
    @GetMapping("/status/{status}")
    public Result<List<UserVO>> getUsersByStatus(@PathVariable Integer status) {
        List<User> users = userService.getByStatus(status);
        List<UserVO> userVOList = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return Result.success(userVOList);
    }
    
    /**
     * 批量更新用户状态
     */
    @PutMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        boolean success = userService.updateStatusByIds(ids, status);
        if (success) {
            return Result.success();
        } else {
            return Result.error("批量更新状态失败");
        }
    }
    
    /**
     * 测试数据库连接
     */
    @GetMapping("/test-connection")
    public Result<String> testConnection() {
        try {
            // 尝试查询用户总数
            long count = userService.count();
            return Result.success("数据库连接成功，当前用户总数：" + count);
        } catch (Exception e) {
            return Result.error("数据库连接失败：" + e.getMessage());
        }
    }
    
    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
