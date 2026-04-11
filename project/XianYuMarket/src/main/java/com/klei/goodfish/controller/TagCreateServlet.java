package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.dto.TagCreateDTO;
import com.klei.goodfish.service.TagService;
import com.klei.goodfish.service.impl.TagServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;
import com.klei.goodfish.vo.TagVO;

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
 * 创建标签（登录用户均可创建）
 * POST /tag/create
 * Body: {"tagName":"数码产品"}
 */
@WebServlet("/tag/create")
public class TagCreateServlet extends HttpServlet {

    private TagService tagService = new TagServiceImpl();
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

            if (userId == null) {
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            // 读取JSON
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析
            TagCreateDTO dto = gson.fromJson(sb.toString(), TagCreateDTO.class);
            dto.setUserId(userId); // 安全处理：以Session为准

            if (dto.getTagName() == null || dto.getTagName().trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "标签名称不能为空").toJson());
                return;
            }

            // 调用Service创建标签
            TagVO vo = tagService.createTag(dto);

            out.print(ResultUtil.success("创建成功", vo).toJson());

        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "创建标签失败：" + e.getMessage()).toJson());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(405);
        resp.getWriter().print(ResultUtil.fail(405, "请使用POST方法").toJson());
    }
}