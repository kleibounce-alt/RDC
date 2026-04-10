package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.GoodUpdateDTO;
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
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 编辑商品（需登录，只能编辑自己的商品）
 * POST /good/update
 * Body: {"goodId":1,"goodName":"iPhone15 Pro","image":"http://xxx.jpg","price":6999.00,"description":"全新"}
 */
@WebServlet("/good/update")
public class GoodUpdateServlet extends HttpServlet {

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

            // 读取 JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 转 DTO
            GoodUpdateDTO dto = gson.fromJson(sb.toString(), GoodUpdateDTO.class);

            // 安全处理：userId 以 Session 为准（防止伪造权限）
            dto.setUserId(userId);

            // 调用 Service
            Good good = goodService.updateGood(dto);

            out.print(ResultUtil.success("编辑成功", good).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "编辑失败：" + e.getMessage()).toJson());
        }
    }
}