package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.User;
import com.zhongjia.biz.service.UserService;
import com.zhongjia.web.req.UserCreateReq;
import com.zhongjia.web.req.UserUpdateReq;
import com.zhongjia.web.vo.Result;
import com.zhongjia.web.exception.ErrorCode;
import com.zhongjia.web.vo.UserVO;
import com.zhongjia.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * 注意：医院、科室字段为Beta功能
 */
@RestController
@Tag(name = "用户管理")
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 创建用户
     */
    @PostMapping("/create")
    @Operation(summary = "创建用户", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<UserVO> createUser(@Validated @RequestBody UserCreateReq req) {
        // 检查用户名是否已存在
        User existingUser = userService.getByUsername(req.getUsername());
        if (existingUser != null) {
            return Result.error(ErrorCode.BAD_REQUEST, "用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(req, user);
        // 简易：入库保存为MD5（演示）。生产建议BCrypt。
        if (req.getPassword() != null) {
            String md5 = org.springframework.util.DigestUtils.md5DigestAsHex(req.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            user.setPassword(md5);
        }
        boolean success = userService.save(user);
        
        if (success) {
            UserVO userVO = userMapper.toVO(user);
            return Result.success(userVO);
        } else {
            return Result.error(ErrorCode.INTERNAL_ERROR, "创建用户失败");
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<UserVO> updateUser(@Validated @RequestBody UserUpdateReq req) {
        User user = userService.getById(req.getId());
        if (user == null) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(req, user);
        boolean success = userService.updateById(user);
        
        if (success) {
            UserVO userVO = userMapper.toVO(user);
            return Result.success(userVO);
        } else {
            return Result.error(ErrorCode.INTERNAL_ERROR, "更新用户失败");
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error(ErrorCode.INTERNAL_ERROR, "删除用户失败");
        }
    }
    
    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserVO userVO = userMapper.toVO(user);
        return Result.success(userVO);
    }
    
    
    
    /**
     * 根据状态查询用户
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询用户", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<List<UserVO>> getUsersByStatus(@Parameter(description = "状态：1启用/0禁用") @PathVariable Integer status) {
        List<User> users = userService.getByStatus(status);
        List<UserVO> userVOList = userMapper.toVOList(users);
        
        return Result.success(userVOList);
    }
    
}
