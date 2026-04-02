package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    // 模拟数据库存储（用户名 -> 密码）
    private static final Map<String, String> userDb = new HashMap<>();

    static {
        // 初始化一个测试用户
        userDb.put("admin", "123");
    }

    @Override
    public Result<String> register(UserDTO userDTO) {
        // 1. 校验用户是否已存在
        if (userDb.containsKey(userDTO.getUsername())) {
            return Result.error(ResultCode.USER_HAS_EXISTED.getCode(), ResultCode.USER_HAS_EXISTED.getMsg());
        }

        // 2. 将新用户存入模拟数据库
        userDb.put(userDTO.getUsername(), userDTO.getPassword());

        // 3. 生成模拟Token
        String token = UUID.randomUUID().toString();

        // 修改：success 只接受一个参数（data），返回的 msg 固定为"操作成功"
        return Result.success(token);
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        // 1. 校验用户是否存在
        if (!userDb.containsKey(userDTO.getUsername())) {
            return Result.error(ResultCode.USER_NOT_EXIST.getCode(), ResultCode.USER_NOT_EXIST.getMsg());
        }

        // 2. 校验密码是否正确
        String dbPassword = userDb.get(userDTO.getUsername());
        if (!dbPassword.equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMsg());
        }

        // 3. 生成模拟Token
        String token = UUID.randomUUID().toString();

        // 修改：success 只接受一个参数（data），返回的 msg 固定为"操作成功"
        return Result.success(token);
    }
}