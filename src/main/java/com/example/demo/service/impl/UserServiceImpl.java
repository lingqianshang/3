package com.example.demo.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserInfo;
import com.example.demo.mapper.UserInfoMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String CACHE_KEY_PREFIX = "user:detail:";

    // ==================== 注册 ====================
    @Override
    public Result<String> register(UserDTO userDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED.getCode(),
                    ResultCode.USER_HAS_EXISTED.getMsg());
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    // ==================== 登录（返回 JWT）====================
    @Override
    public Result<String> login(UserDTO userDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }

        if (!user.getPassword().equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(),
                    ResultCode.PASSWORD_ERROR.getMsg());
        }

        // 生成 JWT Token
        String jwt = jwtUtil.generateToken(userDTO.getUsername());
        return Result.success(jwt);
    }

    // ==================== 根据ID查询用户 ====================
    @Override
    public Result<String> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        return Result.success("查询成功，用户名：" + user.getUsername());
    }

    // ==================== 分页查询 ====================
    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page, null);
        return Result.success(result);
    }

    // ==================== 用户详情（多表联查 + Redis缓存）====================
    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            try {
                UserDetailVO vo = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(vo);
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }
        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(detail), 10, TimeUnit.MINUTES);
        return Result.success(detail);
    }

    // ==================== 更新用户扩展信息 ====================
    @Override
    @Transactional
    public Result<String> updateUserInfo(Long userId, UserInfo userInfo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userId);
        UserInfo existing = userInfoMapper.selectOne(wrapper);
        userInfo.setUserId(userId.intValue());
        if (existing != null) {
            userInfo.setId(existing.getId());
            userInfoMapper.updateById(userInfo);
        } else {
            userInfoMapper.insert(userInfo);
        }
        redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        return Result.success("更新用户信息成功");
    }

    // ==================== 删除用户 ====================
    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        int rows = userMapper.deleteById(userId);
        if (rows == 0) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userId);
        userInfoMapper.delete(wrapper);
        redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        return Result.success("删除用户成功");
    }
}