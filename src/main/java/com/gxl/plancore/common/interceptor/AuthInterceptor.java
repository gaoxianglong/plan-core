package com.gxl.plancore.common.interceptor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson2.JSON;
import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.user.application.service.AuthService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 * 对非白名单请求验证 accessToken 的有效性
 *
 * @author gxl
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("请求缺少Authorization头或格式不正确, uri={}", request.getRequestURI());
            writeUnauthorizedResponse(response);
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = authService.validateAccessToken(token);
            // 将用户信息放入请求属性，供下游使用
            request.setAttribute("userId", claims.getSubject());
            request.setAttribute("sessionId", claims.get("sessionId", String.class));
            request.setAttribute("deviceId", claims.get("deviceId", String.class));
            return true;
        } catch (BusinessException e) {
            log.warn("accessToken验证失败, uri={}", request.getRequestURI());
            writeUnauthorizedResponse(response);
            return false;
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = ApiResponse.error(ErrorCode.UNAUTHORIZED);
        response.getWriter().write(JSON.toJSONString(apiResponse));
    }
}
