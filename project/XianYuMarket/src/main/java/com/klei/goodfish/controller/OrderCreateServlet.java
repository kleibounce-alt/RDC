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

/**
 * 创建订单（购买商品）
 * POST /order/create
 * Body: {"goodId":1}
 * 自动从 Session 获取 buyerId，扣款并创建订单
 * @author klei
 */
@WebServlet("/order/create")
public class OrderCreateServlet extends HttpServlet {

    private OrderService orderService = new OrderServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 Session 获取当前登录用户ID（买家）
            HttpSession session = req.getSession();
            Integer buyerId = (Integer) session.getAttribute("userId");

            // 读取 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转 DTO
            OrderCreateDTO dto = gson.fromJson(sb.toString(), OrderCreateDTO.class);
            dto.setBuyerId(buyerId); // 安全处理：以 Session 为准

            // 调用 Service（内部会扣款、改商品状态为"已出售"）
            Order order = orderService.createOrder(dto);

            out.print(ResultUtil.success("下单成功，等待卖家确认", order).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "下单失败：" + e.getMessage()).toJson());
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