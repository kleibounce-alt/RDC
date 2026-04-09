package com.klei.goodfish.controller;

import com.klei.goodfish.dto.UserRegisterDTO;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.service.impl.UserServiceImpl;
import com.klei.goodfish.vo.UserRegisterVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. 设置编码（防止中文乱码）
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");

        // 2. 获取前端参数（表单提交）
        String userName = req.getParameter("userName");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String roleStr = req.getParameter("role");

        // 3. 组装 DTO
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUserName(userName);
        dto.setPassword(password);
        dto.setConfirmPassword(confirmPassword);
        dto.setRole(roleStr == null ? 0 : Integer.parseInt(roleStr));

        // 4. 调用 Service 注册
        UserRegisterVO vo = userService.register(dto);

        // 5. 返回结果（最简版：纯文本）
        PrintWriter out = resp.getWriter();
        if (vo.isSuccess()) {
            // 注册成功，写 Session
            req.getSession().setAttribute("userId", vo.getUserId());
            out.println("注册成功！欢迎 " + vo.getUserName());
        } else {
            // 注册失败
            out.println("注册失败：" + vo.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // GET 请求提示用 POST
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("请使用 POST 方法提交注册信息");
    }
}