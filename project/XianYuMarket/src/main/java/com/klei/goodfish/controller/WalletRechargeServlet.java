package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.WalletRechargeDTO;
import com.klei.goodfish.service.WalletService;
import com.klei.goodfish.service.impl.WalletServiceImpl;
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
 * 钱包充值（需登录）
 * POST /wallet/recharge
 * Body: {"amount":100.00, "remark":"支付宝充值"}
 * @author klei
 */
@WebServlet("/wallet/recharge")
public class WalletRechargeServlet extends HttpServlet {

    private WalletService walletService = new WalletServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            WalletRechargeDTO dto = gson.fromJson(sb.toString(), WalletRechargeDTO.class);
            dto.setUserId(userId); // 安全处理

            boolean success = walletService.recharge(dto);

            if (success) {
                out.print(ResultUtil.success("充值成功", null).toJson());
            } else {
                resp.setStatus(500);
                out.print(ResultUtil.fail(500, "充值失败").toJson());
            }

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "充值失败：" + e.getMessage()).toJson());
        }
    }
}