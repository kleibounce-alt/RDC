package com.klei.goodfish.controller;

import com.google.gson.Gson;
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

/**
 * 删除评论（需登录，只能删自己的或管理员）
 * POST /comment/delete
 * Body: {"commentId":1}
 * @author klei
 */
@WebServlet("/comment/delete")
public class CommentDeleteServlet extends HttpServlet {

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

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 简单解析 JSON 获取 commentId
            String json = sb.toString();
            Integer commentId = null;
            if (json.contains("\"commentId\"")) {
                String value = json.replaceAll(".*\"commentId\"\\s*:\\s*(\\d+).*", "$1");
                commentId = Integer.parseInt(value);
            }

            if (commentId == null) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "评论ID不能为空").toJson());
                return;
            }

            boolean success = commentService.deleteComment(commentId, userId);

            if (success) {
                out.print(ResultUtil.success("删除成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "删除失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "删除失败：" + e.getMessage()).toJson());
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