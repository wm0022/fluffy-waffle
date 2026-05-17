package com.shengwei.tushuguanli.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证拦截器
 * 校验每个请求头中的 Bearer Token，解析用户身份并存入请求属性供 Controller 使用
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 公开接口：GET /book/{id} 图书详情 - 允许未登录用户查看
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length()); // 去掉 context-path (/api)
        if ("GET".equalsIgnoreCase(method) && isBookDetailPath(path)) {
            return true; // 放行，不校验 Token
        }

        String token = resolveToken(request);

        if (token == null || !jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        // 将用户 ID 和用户名存入请求属性，供后续 Controller 获取当前用户
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);

        return true;
    }

    /**
     * 判断是否为图书详情公开接口（GET /book/{id}）
     * 匹配 /book/{数字ID} 格式，排除 /book/page、/book/hot 等已有白名单的路径
     */
    private boolean isBookDetailPath(String path) {
        return path.matches("^/book/\\d+$");
    }

    /**
     * 从请求头中提取 Bearer Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
