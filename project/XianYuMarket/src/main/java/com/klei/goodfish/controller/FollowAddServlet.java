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

/**
 * 关注用户（需登录）
 * POST /follow/add
 * Body: {"followingId":2}  // 被关注者ID
 * @author klei
 */
@WebServlet("/follow/add")
public class FollowAddServlet extends HttpServlet {

    private FollowService followService = new FollowServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID（关注者）
            HttpSession session = req.getSession(false);
            if (session == null) {
                System.err.println("[FollowAdd] Session is null");
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            Integer followerId = (Integer) session.getAttribute("userId");
            if (followerId == null) {
                System.err.println("[FollowAdd] userId in session is null");
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            System.out.println("[FollowAdd] Current userId from session: " + followerId);

            // 读取 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String jsonBody = sb.toString();
            System.out.println("[FollowAdd] Request body: " + jsonBody);

            // 转DTO
            FollowAddDTO dto = gson.fromJson(jsonBody, FollowAddDTO.class);
            dto.setFollowerId(followerId); // 安全处理：以 Session 为准

            System.out.println("[FollowAdd] DTO: followerId=" + dto.getFollowerId() + ", followingId=" + dto.getFollowingId());

            // 参数校验
            if (dto.getFollowingId() == null) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "被关注者ID不能为空").toJson());
                return;
            }

            if (dto.getFollowerId().equals(dto.getFollowingId())) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "不能关注自己").toJson());
                return;
            }

            // 调用 Service
            boolean success = followService.follow(dto);

            if (success) {
                out.print(ResultUtil.success("关注成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "关注失败").toJson());
            }

        } catch (BusinessException e) {
            System.err.println("[FollowAdd] BusinessException: " + e.getMessage());
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            System.err.println("[FollowAdd] Exception: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误: " + e.getMessage()).toJson());
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