package com.shengwei.tushuguanli.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web 配置 - 静态资源映射 + 拦截器注册（JWT认证 + RBAC权限校验）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    /**
     * 不需要认证的路径白名单（公开接口）
     * 注意：因 server.servlet.context-path=/api，此处路径为去除context-path后的Servlet路径
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/book/page",       // 客户端图书浏览（分页列表）
            "/book/hot",        // 热销图书
            "/book/new",        // 新品图书
            "/book/charity",    // 公益专区
            "/book/count",      // 图书统计
            "/book/isbn/",     // ISBN 查询（含路径变量）
            "/donation/all",    // 捐赠公示
            "/donation/count",  // 捐赠统计
            "/uploads/**"       // 静态资源（图书封面等图片）免认证
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT 认证拦截器（先执行，校验登录状态）
        // 使用 /** 匹配所有 Servlet 路径（因为 context-path 已由 Spring 自动剥离）
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(buildWhiteList())
                .order(1);  // 认证优先

        // 2. RBAC 权限校验拦截器（后执行，校验接口权限）
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .order(2);  // 授权在认证之后
    }

    /**
     * 白名单路径无需再加 /api 前缀（拦截器工作在 Servlet 层，路径已剥离 context-path）
     */
    private String[] buildWhiteList() {
        return WHITE_LIST.toArray(new String[0]);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射图书封面图片
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .addResourceLocations("classpath:/static/uploads/");
    }
}
