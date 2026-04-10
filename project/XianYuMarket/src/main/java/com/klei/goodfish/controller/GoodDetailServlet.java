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

/**
 * 查询商品详情（无需登录，白名单接口）
 * GET /good/detail?id=1
 * @author klei
 */
@WebServlet("/good/detail")
public class GoodDetailServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 URL 参数获取商品ID
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "商品ID不能为空").toJson());
                return;
            }

            Integer goodId = Integer.parseInt(idStr);

            // 调用 Service 查询
            Good good = goodService.getGoodDetail(goodId);

            // 返回成功
            out.print(ResultUtil.success("查询成功", good).toJson());

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