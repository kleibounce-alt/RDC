package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.OrderConfirmDTO;
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

/**
 * 取消订单（买家或卖家取消，退款给买家，商品回库）
 * POST /order/cancel
 * Body: {"orderId":1}
 * 只有待确认的订单能取消，取消后退款
 * @author klei
 */
@WebServlet("/order/cancel")
public class OrderCancelServlet extends HttpServlet {

    private OrderService orderService = new OrderServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID（用于权限校验：买家或卖家都能取消）
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            // 读取 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转 DTO（复用 OrderConfirmDTO，字段一样）
            OrderConfirmDTO dto = gson.fromJson(sb.toString(), OrderConfirmDTO.class);
            dto.setUserId(userId); // 安全处理：以 Session 为准

            // 调用 Service（内部退款给买家，改商品状态为"未出售"，改订单状态为已取消）
            boolean success = orderService.cancelOrder(dto);

            if (success) {
                out.print(ResultUtil.success("取消成功，已退款", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "取消失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "取消失败：" + e.getMessage()).toJson());
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