package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.UserRegisterDTO;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.service.impl.UserServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.UserRegisterVO;

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
 * 用户注册
 * POST /user/register
 * Body: {"userName":"xxx","password":"xxx","confirmPassword":"xxx","phone":"xxx","role":0}
 * @author klei
 */
@WebServlet("/user/register")
public class UserRegisterServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 读 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转 DTO
            UserRegisterDTO dto = gson.fromJson(sb.toString(), UserRegisterDTO.class);

            // 调 Service（返回 UserRegisterVO）
            UserRegisterVO vo = userService.register(dto);

            // 成功则写 Session（自动登录）
            if (vo.isSuccess()) {
                HttpSession session = req.getSession();
                session.setAttribute("userId", vo.getUserId());
            }

            // 返回统一格式
            if (vo.isSuccess()) {
                out.print(ResultUtil.success(vo.getMessage(), vo).toJson());
            } else {
                resp.setStatus(400);
                out.print(ResultUtil.fail(vo.getMessage()).toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误").toJson());
        }
    }
}