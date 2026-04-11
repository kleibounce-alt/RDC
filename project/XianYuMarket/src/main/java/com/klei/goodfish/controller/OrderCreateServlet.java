package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.OrderCreateDTO;
import com.klei.goodfish.entity.Order;
import com.klei.goodfish.service.OrderService;
import com.klei.goodfish.service.impl.OrderServiceImpl;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建订单（购买商品）
 * POST /order/create
 * Body: {"goodId":1}
 */
@WebServlet("/order/create")
public class OrderCreateServlet extends HttpServlet {

    private OrderService orderService = new OrderServiceImpl();
    private Gson gson = new Gson();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            HttpSession session = req.getSession();
            Integer buyerId = (Integer) session.getAttribute("userId");

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            OrderCreateDTO dto = gson.fromJson(sb.toString(), OrderCreateDTO.class);
            dto.setBuyerId(buyerId);

            Order order = orderService.createOrder(dto);

            // 关键：转换为 Map，处理 LocalDateTime
            Map<String, Object> result = new HashMap<>();
            result.put("id", order.getId());
            result.put("goodId", order.getGoodId());
            result.put("buyerId", order.getBuyerId());
            result.put("sellerId", order.getSellerId());
            result.put("price", order.getPrice());
            result.put("status", order.getStatus());
            // 0待确认 1已完成 2已取消
            result.put("statusName", getStatusName(order.getStatus()));

            if (order.getCreateTime() != null) {
                result.put("createTime", order.getCreateTime().format(formatter));
            } else {
                result.put("createTime", "");
            }

            out.print(ResultUtil.success("下单成功，等待卖家确认", result).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "下单失败：" + e.getMessage()).toJson());
        }
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待确认";
            case 1: return "已完成";
            case 2: return "已取消";
            default: return "未知";
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