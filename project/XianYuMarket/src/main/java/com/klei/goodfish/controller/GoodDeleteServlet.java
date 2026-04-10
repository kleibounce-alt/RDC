package com.klei.goodfish.controller;

import com.google.gson.Gson;
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
 * 删除商品（逻辑删除，需登录，只能删除自己的商品或管理员）
 * POST /good/delete
 * Body: {"goodId":1}
 * @author klei
 */
@WebServlet("/good/delete")
public class GoodDeleteServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
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

            // 读取 JSON 获取 goodId
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 简单解析 JSON 获取 goodId
            String json = sb.toString();
            Integer goodId = null;
            if (json.contains("\"goodId\"")) {
                String value = json.replaceAll(".*\"goodId\"\\s*:\\s*(\\d+).*", "$1");
                goodId = Integer.parseInt(value);
            }

            if (goodId == null) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "商品ID不能为空").toJson());
                return;
            }

            // 调用 Service 删除
            boolean success = goodService.deleteGood(goodId, userId);

            if (success) {
                out.print(ResultUtil.success("删除成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "删除失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "删除失败：" + e.getMessage()).toJson());
        }
    }
}