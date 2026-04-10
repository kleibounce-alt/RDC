package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.GoodPublishDTO;
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
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 发布商品（需登录）
 * POST /good/publish
 * Body: {"goodName":"iPhone15","image":"http://xxx.jpg","price":5999.00,"description":"九成新"}
 * sellerId 从 Session 自动获取（安全考虑，不信任前端传的userId）
 */
@WebServlet("/good/publish")
public class GoodPublishServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID（卖家）
            HttpSession session = req.getSession();
            Integer sellerId = (Integer) session.getAttribute("userId");

            // 读取 JSON 请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转换为 DTO
            GoodPublishDTO dto = gson.fromJson(sb.toString(), GoodPublishDTO.class);

            // 安全处理：sellerId 以 Session 为准，忽略前端传的（防止伪造）
            dto.setSellerId(sellerId);

            // 调用 Service 发布商品（返回 Good 实体）
            Good good = goodService.publishGood(dto);

            // 返回成功（包含创建的商品信息，含自增ID）
            out.print(ResultUtil.success("发布成功", good).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "发布失败：" + e.getMessage()).toJson());
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