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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
            System.out.println("[CommentList] 收到请求，goodId参数: " + goodIdStr);

            if (goodIdStr == null || goodIdStr.trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "商品ID不能为空").toJson());
                return;
            }

            Integer goodId = Integer.parseInt(goodIdStr);

            // 获取当前用户ID（可能为null，表示未登录）
            HttpSession session = req.getSession(false);
            Integer currentUserId = null;
            if (session != null) {
                currentUserId = (Integer) session.getAttribute("userId");
            }

            System.out.println("[CommentList] 查询商品评论，goodId=" + goodId + ", currentUserId=" + currentUserId);

            // 传入currentUserId（允许为null）
            List<CommentVO> list = commentService.getCommentsByGoodId(goodId, currentUserId);

            System.out.println("[CommentList] 查询完成，返回给前端的评论数: " + (list != null ? list.size() : 0));

            // 关键新增：详细日志，打印前3条评论数据
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < Math.min(list.size(), 3); i++) {
                    CommentVO vo = list.get(i);
                    System.out.println("[CommentList] 评论 " + i + ": id=" + vo.getCommentId()
                            + ", userId=" + vo.getUserId()
                            + ", userName=" + vo.getUserName()
                            + ", content=" + (vo.getContent() != null ? vo.getContent().substring(0, Math.min(20, vo.getContent().length())) + "..." : "null"));
                }
            } else {
                System.out.println("[CommentList] 警告：返回给前端的列表为空！");
            }

            out.print(ResultUtil.success("查询成功", list).toJson());

        } catch (NumberFormatException e) {
            System.err.println("[CommentList] 参数格式错误: " + e.getMessage());
            resp.setStatus(400);
            out.print(ResultUtil.fail(400, "商品ID格式错误").toJson());
        } catch (BusinessException e) {
            System.err.println("[CommentList] 业务异常: " + e.getMessage());
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            System.err.println("[CommentList] 系统异常: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误: " + e.getMessage()).toJson());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(405);
        resp.getWriter().print(ResultUtil.fail(405, "请使用GET方法").toJson());
    }
}