package com.example.vnollxai.filter;

import com.example.vnollxai.utils.Jwt;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "TokenFilter", urlPatterns = {"/user/*","/message/*"})
public class TokenFilter implements Filter {
    private static final String TOKEN_PARAM = "token";
    private static final String[] EXCLUDED_PATHS = {
            "/user/login",
            "/user/register",
            "/user/update/password"
    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();

        // 路径匹配逻辑
        for (String path : EXCLUDED_PATHS) {
            if (path.contains("\\d+")) {
                if (requestURI.matches("^" + path.replace("\\d+", "\\d+") + "$")) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } else if (requestURI.startsWith(path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Token验证逻辑
        String token = request.getParameter(TOKEN_PARAM);
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("未找到token");
            return;
        }

        if (!Jwt.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("无效token");
            return;
        }

        // 提取用户ID并传递到后续请求
        String userId = Jwt.getUserIdFromToken(token);
        request.setAttribute("uid", userId);
        filterChain.doFilter(request, response);
    }

    // 添加默认的init和destroy方法实现
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}