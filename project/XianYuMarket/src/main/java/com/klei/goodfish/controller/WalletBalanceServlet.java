package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.WalletService;
import com.klei.goodfish.service.impl.WalletServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.WalletBalanceVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 查询钱包余额（需登录）
 * GET /wallet/balance
 * 从 Session 自动获取 userId，无需传参
 * @author klei
 */
@WebServlet("/wallet/balance")
public class WalletBalanceServlet extends HttpServlet {

    private WalletService walletService = new WalletServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId");

            WalletBalanceVO vo = walletService.getBalance(userId);

            out.print(ResultUtil.success("查询成功", vo).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "查询失败：" + e.getMessage()).toJson());
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