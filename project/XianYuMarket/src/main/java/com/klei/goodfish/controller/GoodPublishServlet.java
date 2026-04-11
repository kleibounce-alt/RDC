package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.GoodPublishDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 发布商品（需登录）
 */
@WebServlet("/good/publish")
public class GoodPublishServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
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
            Integer sellerId = (Integer) session.getAttribute("userId");

            if (sellerId == null) {
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            GoodPublishDTO dto = gson.fromJson(sb.toString(), GoodPublishDTO.class);
            dto.setSellerId(sellerId);

            Good good = goodService.publishGood(dto);

            // 转换为 Map，处理 null 值
            Map<String, Object> result = new HashMap<>();
            result.put("id", good.getId());
            result.put("goodName", good.getGoodName());
            result.put("goodImage", good.getGoodImage());
            result.put("goodPrice", good.getGoodPrice());
            result.put("description", good.getDescription());
            result.put("status", good.getStatus());
            result.put("sellerId", good.getSellerId());
            result.put("sellingStatus", good.getSellingStatus());
            // 关键修复：判断 null
            if (good.getCreateTime() != null) {
                result.put("createTime", good.getCreateTime().format(formatter));
            } else {
                result.put("createTime", "");
            }

            out.print(ResultUtil.success("发布成功", result).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "发布失败：" + e.getMessage()).toJson());
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