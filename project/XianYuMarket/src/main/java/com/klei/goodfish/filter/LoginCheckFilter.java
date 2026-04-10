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
@WebFilter("/*")  // 过滤所有，内部判断哪些需要登录
public class LoginCheckFilter implements Filter {

    private Gson gson = new Gson();

    // 不需要登录的白名单（注册、登录、查商品等）
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

        // 获取请求路径（去掉上下文）
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (path.startsWith(context)) {
            path = path.substring(context.length());
        }

        // 白名单直接放行
        if (isWhiteList(path)) {
            chain.doFilter(req, res);
            return;
        }

        // 检查 Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            // 未登录，返回 401
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            PrintWriter out = response.getWriter();
            out.print(ResultUtil.fail(401, "请先登录").toJson());
            return;
        }

        // 已登录，放行
        chain.doFilter(req, res);
    }

    private boolean isWhiteList(String path) {
        // 精确匹配或前缀匹配
        for (String white : WHITE_LIST) {
            if (path.equals(white) || path.startsWith(white + "/")) {
                return true;
            }
        }
        return false;
    }
}