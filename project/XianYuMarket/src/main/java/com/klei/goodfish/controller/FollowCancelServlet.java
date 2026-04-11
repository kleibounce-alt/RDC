package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.FollowAddDTO;
import com.klei.goodfish.service.FollowService;
import com.klei.goodfish.service.impl.FollowServiceImpl;
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

@WebServlet("/follow/cancel")
public class FollowCancelServlet extends HttpServlet {

    private FollowService followService = new FollowServiceImpl();
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

            // 读取JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 前端传的是 {userId: xxx}，需要转成 FollowAddDTO 的字段名
            // 手动解析或使用Map中转
            java.util.Map map = gson.fromJson(sb.toString(), java.util.Map.class);

            FollowAddDTO dto = new FollowAddDTO();
            dto.setFollowerId(userId);  // 当前登录用户（关注者）

            // 前端传的被关注者ID可能是 userId 或 followingId
            Object targetId = map.get("userId");
            if (targetId == null) {
                targetId = map.get("followingId");
            }

            if (targetId == null) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "用户ID不能为空").toJson());
                return;
            }

            dto.setFollowingId(((Number) targetId).intValue());

            // ★★★ 调用你现有的 unfollow 方法 ★★★
            boolean success = followService.unfollow(dto);

            if (success) {
                out.print(ResultUtil.success("取消关注成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "取消关注失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误：" + e.getMessage()).toJson());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(405);
        resp.getWriter().print(ResultUtil.fail(405, "请使用POST方法").toJson());
    }
}