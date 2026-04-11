package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klei.goodfish.service.OrderService;
import com.klei.goodfish.service.impl.OrderServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.OrderVO;

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
import java.util.List;

/**
 * 获取我购买的订单（买家视角）
 * GET /order/purchases
 */
@WebServlet("/order/purchases")
public class OrderPurchasesServlet extends HttpServlet {

    private OrderService orderService = new OrderServiceImpl();

    // 配置 Gson 正确处理 LocalDateTime
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
            // 从 Session 获取当前登录用户ID
            HttpSession session = req.getSession();
            Integer buyerId = (Integer) session.getAttribute("userId");

            if (buyerId == null) {
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            System.out.println("[OrderPurchases] 查询购买订单, buyerId=" + buyerId);

            // 调用 Service 查询购买订单
            List<OrderVO> orders = orderService.getMyPurchases(buyerId);

            System.out.println("[OrderPurchases] 查询结果: " + (orders != null ? orders.size() : 0) + " 条记录");

            out.print(ResultUtil.success("查询成功", orders).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "查询失败: " + e.getMessage()).toJson());
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