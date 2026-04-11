package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.CommentLikeDTO;
import com.klei.goodfish.service.CommentLikeService;
import com.klei.goodfish.service.impl.CommentLikeServiceImpl;
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

@WebServlet("/comment/like")
public class CommentLikeServlet extends HttpServlet {

    private CommentLikeService likeService = new CommentLikeServiceImpl();
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

            // 解析参数
            java.util.Map map = gson.fromJson(sb.toString(), java.util.Map.class);
            Object commentIdObj = map.get("commentId");

            if (commentIdObj == null) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "评论ID不能为空").toJson());
                return;
            }

            Integer commentId = ((Number) commentIdObj).intValue();

            CommentLikeDTO dto = new CommentLikeDTO();
            dto.setCommentId(commentId);
            dto.setUserId(userId);

            boolean success = likeService.like(dto);

            if (success) {
                out.print(ResultUtil.success("点赞成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "点赞失败").toJson());
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