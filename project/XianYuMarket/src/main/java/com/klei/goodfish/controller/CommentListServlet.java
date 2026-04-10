package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.CommentService;
import com.klei.goodfish.service.impl.CommentServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.CommentVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 查询商品评论列表（无需登录，白名单）
 * GET /comment/list?goodId=1
 */
@WebServlet("/comment/list")
public class CommentListServlet extends HttpServlet {

    private CommentService commentService = new CommentServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String goodIdStr = req.getParameter("goodId");
            if (goodIdStr == null || goodIdStr.trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "商品ID不能为空").toJson());
                return;
            }

            Integer goodId = Integer.parseInt(goodIdStr);

            List<CommentVO> list = commentService.getCommentsByGoodId(goodId);

            out.print(ResultUtil.success("查询成功", list).toJson());

        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print(ResultUtil.fail(400, "商品ID格式错误").toJson());
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