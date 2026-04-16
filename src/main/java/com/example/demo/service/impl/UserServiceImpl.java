package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<String> register(UserDTO userDTO) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);

        if (existingUser != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED.getCode(),
                    ResultCode.USER_HAS_EXISTED.getMsg());
        }

        // 2. 创建新用户
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());

        // 3. 保存到数据库
        int result = userMapper.insert(user);

        if (result > 0) {
            return Result.success("注册成功");
        }
        return Result.error(500, "注册失败");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        // 2. 用户不存在
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(),
                    ResultCode.USER_NOT_EXIST.getMsg());
        }

        // 3. 密码错误
        if (!user.getPassword().equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(),
                    ResultCode.PASSWORD_ERROR.getMsg());
        }

        // 4. 登录成功
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
        // 1. 创建分页对象（参数1：当前页码，参数2：每页显示条数）
        Page<User> pageParam = new Page<>(pageNum, pageSize);

        // 2. 执行分页查询（参数2：查询条件Wrapper，传null代表查询全部）
        Page<User> resultPage = userMapper.selectPage(pageParam, null);

        // 3. 返回结果（resultPage中包含了records数据列表、total总条数、pages总页数等）
        return Result.success(resultPage);
    }
}