package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.FavoriteAddDTO;
import com.klei.goodfish.service.FavoriteService;
import com.klei.goodfish.service.impl.FavoriteServiceImpl;
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
 * 添加收藏（需登录）
 * POST /favorite/add
 * Body: {"goodId":1}
 * @author klei
 */
@WebServlet("/favorite/add")
public class FavoriteAddServlet extends HttpServlet {

    private FavoriteService favoriteService = new FavoriteServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            // 读取 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转 DTO
            FavoriteAddDTO dto = gson.fromJson(sb.toString(), FavoriteAddDTO.class);
            dto.setUserId(userId); // 安全处理：以 Session 为准

            // 调用 Service
            boolean success = favoriteService.addFavorite(dto);

            if (success) {
                out.print(ResultUtil.success("收藏成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "收藏失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "收藏失败：" + e.getMessage()).toJson());
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