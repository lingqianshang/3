package com.example.demo.interceptor;

import com.example.demo.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行查询、新增接口
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // GET查询 / POST新增 直接放行
        if ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
            return true;
        }

        // PUT/DELETE 修改删除 → 校验token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":" + ResultCode.NO_AUTH.getCode() + ",\"msg\":\"" + ResultCode.NO_AUTH.getMsg() + "\"}");
            return false;
        }
        return true;
    }
}