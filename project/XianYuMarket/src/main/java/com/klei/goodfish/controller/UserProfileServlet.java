package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.service.impl.UserServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.UserProfileVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 查看个人信息（需登录）
 * GET /user/profile
 * 从 Session 取 userId，无需传参
 * @author klei
 */
@WebServlet("/user/profile")
public class UserProfileServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID（Filter已校验过，这里一定能取到）
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            // 调用 Service 查询个人信息
            UserProfileVO vo = userService.getUser(userId);

            if (vo == null) {
                resp.setStatus(404);
                out.print(ResultUtil.fail(404, "用户不存在").toJson());
                return;
            }

            // 返回成功
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