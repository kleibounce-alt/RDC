package com.klei.goodfish.filter;

import com.klei.goodfish.util.ResultUtil;
import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * 登录校验过滤器 - 拦截需要登录的接口
 * @author klei
 */
@WebFilter("/*")
public class LoginCheckFilter implements Filter {

    private Gson gson = new Gson();

    // 白名单：不需要登录就能访问的接口（都不带 /api 前缀，因为 Vite 已经 rewrite 掉了）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/user/register",
            "/user/login",
            "/good/detail",
            "/good/random",
            "/good/search",
            "/good/list",
            "/tag/list",
            "/comment/list"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // 获取请求路径（标准方式：去掉 contextPath）
        String path = request.getRequestURI();
        String context = request.getContextPath();

        // 去掉上下文路径（如果配置了根路径 /，context 为空，path 不变）
        if (path.startsWith(context)) {
            path = path.substring(context.length());
        }

        System.out.println("[LoginCheckFilter] 路径: " + path + ", 方法: " + request.getMethod());

        // 白名单直接放行
        if (isWhiteList(path)) {
            chain.doFilter(req, res);
            return;
        }

        // 检查 Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            System.out.println("[LoginCheckFilter] 拦截: " + path + " - 未登录");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            PrintWriter out = response.getWriter();
            out.print(ResultUtil.fail(401, "请先登录").toJson());
            return;
        }

        // 已登录放行
        System.out.println("[LoginCheckFilter] 放行: " + path + " - 用户ID: " + session.getAttribute("userId"));
        chain.doFilter(req, res);
    }

    private boolean isWhiteList(String path) {
        for (String white : WHITE_LIST) {
            if (path.equals(white) || path.startsWith(white + "/")) {
                return true;
            }
        }
        return false;
    }
}