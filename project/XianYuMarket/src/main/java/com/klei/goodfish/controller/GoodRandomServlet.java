package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.service.GoodService;
import com.klei.goodfish.service.impl.GoodServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 随机推荐商品（无需登录）
 * GET /good/random?limit=8
 * limit 不传默认返回8个
 * @author klei
 */
@WebServlet("/good/random")
public class GoodRandomServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 获取 limit 参数，不传默认为8
            String limitStr = req.getParameter("limit");
            Integer limit = 8; // 默认值
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                limit = Integer.parseInt(limitStr);
            }

            // 调用 Service
            List<Good> goods = goodService.getRandomGoods(limit);

            out.print(ResultUtil.success("查询成功", goods).toJson());

        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print(ResultUtil.fail(400, "参数格式错误").toJson());
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