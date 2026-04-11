package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klei.goodfish.service.UserService;
import com.klei.goodfish.service.impl.UserServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.UserFavoriteVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * 查看我的收藏列表（需登录）
 * GET /user/favorite
 * @author klei
 */
@WebServlet("/user/favorite")
public class UserFavoriteServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();

    // ★★★ 关键修复：配置 Gson 正确处理 LocalDateTime ★★★
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonSerializer<LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                @Override
                public com.google.gson.JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                    return new com.google.gson.JsonPrimitive(src.format(formatter));
                }
            })
            .create();

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

            System.out.println("[UserFavorite] 查询用户收藏, userId=" + userId);

            UserFavoriteVO vo = userService.getUserFavorite(userId);

            // ★★★ 关键修复：确保返回空数组而不是 null ★★★
            if (vo == null) {
                vo = new UserFavoriteVO();
                vo.setUserId(userId);
                vo.setFavorites(new ArrayList<>());
            }

            if (vo.getFavorites() == null) {
                vo.setFavorites(new ArrayList<>());
            }

            System.out.println("[UserFavorite] 返回收藏数量: " + vo.getFavorites().size());

            // 使用自定义 Gson 序列化
            String jsonResponse = ResultUtil.success("查询成功", vo).toJson();
            out.print(jsonResponse);

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
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
        resp.getWriter().print(ResultUtil.fail(405, "请使用 GET 方法").toJson());
    }
}