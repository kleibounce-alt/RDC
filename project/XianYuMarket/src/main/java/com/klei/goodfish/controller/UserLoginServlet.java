package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.UserLoginDTO;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.service.impl.UserServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.UserLoginVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用户登录
 * POST /user/login
 * Body: {"userName":"xxx","password":"xxx"}
 */
@WebServlet("/user/login")
public class UserLoginServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 读取 JSON 请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转换为 DTO
            UserLoginDTO dto = gson.fromJson(sb.toString(), UserLoginDTO.class);

            // 调用 Service 登录
            UserLoginVO vo = userService.login(dto);

            // 处理结果
            if (vo.isSuccess()) {
                // 登录成功，写入 Session
                HttpSession session = req.getSession();
                session.setAttribute("userId", vo.getUserId());

                out.print(ResultUtil.success(vo.getMessage(), vo).toJson());
            } else {
                // 登录失败（用户名或密码错误）
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, vo.getMessage()).toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误").toJson());
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