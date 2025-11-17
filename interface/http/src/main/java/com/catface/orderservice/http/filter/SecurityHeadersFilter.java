package com.catface.orderservice.http.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 安全响应头过滤器
 * 为所有 HTTP 响应添加安全相关的响应头
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // 添加 X-Content-Type-Options 响应头，防止 MIME 类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // 添加 X-Frame-Options 响应头，防止点击劫持攻击
        response.setHeader("X-Frame-Options", "DENY");
        
        // 添加 X-XSS-Protection 响应头，启用浏览器的 XSS 过滤器
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}
