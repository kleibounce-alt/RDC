package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.GoodSearchDTO;
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
 * 搜索商品（模糊查询，无需登录）
 * GET /good/search?keyword=iPhone
 * @author klei
 */
@WebServlet("/good/search")
public class GoodSearchServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 获取 keyword 参数
            String keyword = req.getParameter("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "搜索关键词不能为空").toJson());
                return;
            }

            // 组装 DTO
            GoodSearchDTO dto = new GoodSearchDTO();
            dto.setKeyword(keyword.trim());

            // 调用 Service
            List<Good> goods = goodService.searchGoods(dto);

            out.print(ResultUtil.success("搜索成功", goods).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "搜索失败：" + e.getMessage()).toJson());
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