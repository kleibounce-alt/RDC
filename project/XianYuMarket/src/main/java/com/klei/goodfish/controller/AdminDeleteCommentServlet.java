package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.AdminDeleteDTO;
import com.klei.goodfish.service.CommentService;
import com.klei.goodfish.service.impl.CommentServiceImpl;
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

@WebServlet("/admin/comment/delete")
public class AdminDeleteCommentServlet extends HttpServlet {

    private CommentService commentService = new CommentServiceImpl();
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
            Integer role = (Integer) session.getAttribute("role");

            if (userId == null) {
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            if (role == null || role != 1) {
                resp.setStatus(403);
                out.print(ResultUtil.fail(403, "无权操作，需要管理员权限").toJson());
                return;
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            AdminDeleteDTO dto = gson.fromJson(sb.toString(), AdminDeleteDTO.class);

            if (dto.getCommentId() == null) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "评论ID不能为空").toJson());
                return;
            }

            boolean success = commentService.adminDeleteComment(dto.getCommentId());

            if (success) {
                out.print(ResultUtil.success("删除评论成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "删除失败，评论不存在").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误：" + e.getMessage()).toJson());
        }
    }
}