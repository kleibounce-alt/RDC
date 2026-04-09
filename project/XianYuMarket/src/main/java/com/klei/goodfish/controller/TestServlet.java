package com.klei.goodfish.controller;

import lombok.NonNull;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/test")
// 配置访问路径：http://localhost:8080/goodfish/test
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 设置编码（防止中文乱码）
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        // 获取参数（比如访问：/test?name=klei）
        String name = req.getParameter("name");
        if (name == null) {
            name = "陌生人";
        }

        // 输出响应
        PrintWriter out = resp.getWriter();
        out.println("<h1>Hello, " + name + "</h1>");
        out.println("<p>当前时间：" + new java.util.Date() + "</p>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // POST 也设置编码（否则中文会乱码）
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        // 获取表单参数
        String userName = req.getParameter("userName");
        String password = req.getParameter("password");

        PrintWriter out = resp.getWriter();
        out.println("<h1>收到 POST 请求</h1>");
        out.println("<p>用户名：" + userName + "</p>");
        out.println("<p>密码：" + password + "</p>");
    }
}