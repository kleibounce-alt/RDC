package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.FollowService;
import com.klei.goodfish.service.impl.FollowServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.FansVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/follow/fans")
public class FollowFansServlet extends HttpServlet {

    private FollowService followService = new FollowServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
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

            // 获取当前用户的粉丝列表（谁关注了我）
            List<FansVO> fans = followService.getMyFans(userId);

            out.print(ResultUtil.success("查询成功", fans).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "查询失败：" + e.getMessage()).toJson());
        }
    }
}