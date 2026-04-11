package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.FavoriteAddDTO;  // ★★★ 改用 FavoriteAddDTO
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

@WebServlet("/favorite/cancel")
public class FavoriteCancelServlet extends HttpServlet {

    private FavoriteService favoriteService = new FavoriteServiceImpl();
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

            // ★★★ 使用 FavoriteAddDTO（包含 userId 和 goodId）
            FavoriteAddDTO dto = gson.fromJson(sb.toString(), FavoriteAddDTO.class);
            dto.setUserId(userId);  // 从 Session 注入当前用户

            if (dto.getGoodId() == null) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "商品ID不能为空").toJson());
                return;
            }

            // 调用 Service 的 cancelFavorite（参数是 FavoriteAddDTO）
            boolean success = favoriteService.cancelFavorite(dto);

            if (success) {
                out.print(ResultUtil.success("取消收藏成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "取消收藏失败").toJson());
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