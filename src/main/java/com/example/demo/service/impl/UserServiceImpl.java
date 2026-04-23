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

    private static final String CACHE_KEY_PREFIX = "user:detail:";

    // ==================== 任务4/5/6 方法实现 ====================

    @Override
    public Result<String> register(UserDTO userDTO) {
        // 检查用户名是否存在
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
        return Result.success("登录成功");
    }

    @Override
    public Result<String> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        return Result.success("查询成功，用户名：" + user.getUsername());
    }

    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page, null);
        return Result.success(result);
    }

    // ==================== 任务7 新增方法 ====================

    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        // 1. 查缓存
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            try {
                UserDetailVO vo = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(vo);
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }
        // 2. 查数据库（多表联查）
        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        // 3. 写缓存，过期时间10分钟
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(detail), 10, TimeUnit.MINUTES);
        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<String> updateUserInfo(Long userId, UserInfo userInfo) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        // 查询是否已有扩展信息
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
        // 删除缓存
        redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        return Result.success("更新用户信息成功");
    }

    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        // 删除主表
        int rows = userMapper.deleteById(userId);
        if (rows == 0) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }
        // 删除扩展信息
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userId);
        userInfoMapper.delete(wrapper);
        // 删除缓存
        redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        return Result.success("删除用户成功");
    }
}