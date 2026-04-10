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
 * 确认订单（卖家确认收款，交易完成）
 * POST /order/confirm
 * Body: {"orderId":1}
 * 只有卖家能确认，确认后钱打入卖家钱包
 */
@WebServlet("/order/confirm")
public class OrderConfirmServlet extends HttpServlet {

    private OrderService orderService = new OrderServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID（用于权限校验）
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            // 读取 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转 DTO
            OrderConfirmDTO dto = gson.fromJson(sb.toString(), OrderConfirmDTO.class);
            dto.setUserId(userId); // 安全处理：以 Session 为准

            // 调用 Service（内部给卖家打钱，改订单状态为已完成）
            boolean success = orderService.confirmOrder(dto);

            if (success) {
                out.print(ResultUtil.success("确认成功，交易完成", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "确认失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "确认失败：" + e.getMessage()).toJson());
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