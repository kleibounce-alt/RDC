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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 随机推荐商品（无需登录）
 */
@WebServlet("/good/random")
public class GoodRandomServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
    private Gson gson = new Gson();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String limitStr = req.getParameter("limit");
            Integer limit = 8;
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                limit = Integer.parseInt(limitStr);
            }

            List<Good> goods = goodService.getRandomGoods(limit);

            // 转换为 List<Map>，处理 null 值
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Good good : goods) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", good.getId());
                map.put("goodName", good.getGoodName());
                map.put("goodImage", good.getGoodImage());
                map.put("goodPrice", good.getGoodPrice());
                map.put("description", good.getDescription());
                map.put("status", good.getStatus());
                map.put("sellerId", good.getSellerId());
                map.put("sellingStatus", good.getSellingStatus());
                // 关键修复：判断 null
                if (good.getCreateTime() != null) {
                    map.put("createTime", good.getCreateTime().format(formatter));
                } else {
                    map.put("createTime", "");
                }
                resultList.add(map);
            }

            out.print(ResultUtil.success("查询成功", resultList).toJson());

        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print(ResultUtil.fail(400, "参数格式错误").toJson());
        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
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