package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 查询单个用户
    @GetMapping("/{id}")
    public Result<String> getUserById(@PathVariable Long id) {
        return Result.success("查询成功，用户ID：" + id);
    }

    // 新增用户（需要请求体）
    @PostMapping
    public Result<String> addUser(@RequestBody User user) {
        return Result.success("新增成功，用户：" + user.getName());
    }

    // 修改用户
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        return Result.success("修改成功，用户ID：" + id);
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        return Result.success("删除成功，用户ID：" + id);
    }
}