package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.WalletService;
import com.klei.goodfish.service.impl.WalletServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.WalletLogVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 查询钱包流水记录（需登录）
 * GET /wallet/logs?type=1
 * type 可选：1充值 2购买 3收入，不传查全部
 * @author klei
 */
@WebServlet("/wallet/logs")
public class WalletLogsServlet extends HttpServlet {

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

            // 获取可选参数 type
            String typeStr = req.getParameter("type");
            List<WalletLogVO> logs;

            if (typeStr != null && !typeStr.trim().isEmpty()) {
                Integer type = Integer.parseInt(typeStr);
                logs = walletService.getWalletLogsByType(userId, type);
            } else {
                logs = walletService.getWalletLogs(userId);
            }

            out.print(ResultUtil.success("查询成功", logs).toJson());

        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print(ResultUtil.fail(400, "type参数格式错误").toJson());
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