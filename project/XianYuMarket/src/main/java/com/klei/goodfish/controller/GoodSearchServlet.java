package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.GoodSearchDTO;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索商品（模糊查询，无需登录）
 * GET /good/search?keyword=iPhone
 * @author klei
 */
@WebServlet("/good/search")
public class GoodSearchServlet extends HttpServlet {

    private GoodService goodService = new GoodServiceImpl();
    private Gson gson = new Gson();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 获取 keyword 参数
            String keyword = req.getParameter("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "搜索关键词不能为空").toJson());
                return;
            }

            // 组装 DTO
            GoodSearchDTO dto = new GoodSearchDTO();
            dto.setKeyword(keyword.trim());

            // 调用 Service
            List<Good> goods = goodService.searchGoods(dto);

            // 关键：转换为 Map 列表，处理 LocalDateTime 和 null 值
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Good good : goods) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", good.getId());
                map.put("goodName", good.getGoodName());
                map.put("goodImage", good.getGoodImage());
                map.put("goodPrice", good.getGoodPrice());
                map.put("description", good.getDescription());
                map.put("status", good.getStatus());
                map.put("sellerId", good.getSellerId());
                map.put("sellingStatus", good.getSellingStatus());
                // 处理 null 值
                if (good.getCreateTime() != null) {
                    map.put("createTime", good.getCreateTime().format(formatter));
                } else {
                    map.put("createTime", "");
                }
                resultList.add(map);
            }

            out.print(ResultUtil.success("搜索成功", resultList).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "搜索失败：" + e.getMessage()).toJson());
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