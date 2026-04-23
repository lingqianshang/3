package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.UserInfo;
import com.example.demo.vo.UserDetailVO;

public interface UserService {
    // 任务4/5/6 原有方法
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);
    Result<String> getUserById(Long id);
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);

    // 任务7 新增方法
    Result<UserDetailVO> getUserDetail(Long userId);
    Result<String> updateUserInfo(Long userId, UserInfo userInfo);
    Result<String> deleteUser(Long userId);
}