package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.service.impl.UserServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.UserFollowVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 查看我的关注列表（需登录）
 * GET /user/follow
 * 从 Session 取 userId，无需传参
 */
@WebServlet("/user/follow")
public class UserFollowServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            // 调用 Service
            UserFollowVO vo = userService.getUserFollow(userId);

            if (vo == null) {
                // 没有关注记录
                out.print(ResultUtil.success("暂无关注记录", null).toJson());
                return;
            }

            out.print(ResultUtil.success("查询成功", vo).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误").toJson());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(405);
        resp.getWriter().print(ResultUtil.fail(405, "请使用 GET 方法").toJson());
    }
}