package com.example.demo.common;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器异常"),
    NO_AUTH(401, "未授权，请登录"),

    // 用户相关业务状态码（实验任务4追加）
    USER_HAS_EXISTED(4001, "该用户名已被注册"),
    USER_NOT_EXIST(4002, "该用户不存在"),
    PASSWORD_ERROR(4003, "账号或密码错误");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}