package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.service.TagService;
import com.klei.goodfish.service.impl.TagServiceImpl;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.TagVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 获取标签列表（无需登录）
 * GET /tag/list
 */
@WebServlet("/tag/list")
public class TagListServlet extends HttpServlet {

    private TagService tagService = new TagServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            List<TagVO> tags = tagService.getAllTags();
            out.print(ResultUtil.success("查询成功", tags).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "查询标签失败: " + e.getMessage()).toJson());
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