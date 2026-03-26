package com.example.demo.common;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器异常"),
    NO_AUTH(401, "未授权，请登录");

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