package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.service.impl.UserServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 更新用户头像
 * POST /user/updateAvatar
 * Body: {"avatar":"http://localhost:8080/uploads/xxx.jpg"}
 * @author klei
 */
@WebServlet("/user/updateAvatar")
public class UserUpdateAvatarServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            if (userId == null) {
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            // 读取 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析 avatar URL
            Map<String, String> map = gson.fromJson(sb.toString(), Map.class);
            String avatarUrl = map.get("avatar");

            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "头像URL不能为空").toJson());
                return;
            }

            // 调用 Service 更新头像
            boolean success = userService.updateAvatar(userId, avatarUrl.trim());

            if (success) {
                out.print(ResultUtil.success("头像更新成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "头像更新失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误：" + e.getMessage()).toJson());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(405);
        resp.getWriter().print(ResultUtil.fail(405, "请使用 POST 方法").toJson());
    }
}