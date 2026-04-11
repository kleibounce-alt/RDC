package com.klei.goodfish.controller;

import com.google.gson.Gson;
import com.klei.goodfish.entity.Good;
import com.klei.goodfish.entity.User;
import com.klei.goodfish.mapper.UserMapper;
import com.klei.goodfish.mappercore.proxy.MapperProxy;
import com.klei.goodfish.service.FollowService;
import com.klei.goodfish.service.impl.FollowServiceImpl;
import com.klei.goodfish.util.BusinessException;
import com.klei.goodfish.util.ResultUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询商品详情（无需登录）
 * GET /good/detail?id=1
 */
@WebServlet("/good/detail")
public class GoodDetailServlet extends HttpServlet {

    private Gson gson = new Gson();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private UserMapper userMapper = MapperProxy.getMapper(UserMapper.class);
    private FollowService followService = new FollowServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "商品ID不能为空").toJson());
                return;
            }

            Integer goodId = Integer.parseInt(idStr);

            // 查询商品（复用现有的 mapper 查询，避免循环依赖）
            // 直接用 mapper 查询，不经过 service 层，避免 service 的权限检查
            com.klei.goodfish.mapper.GoodMapper goodMapper = MapperProxy.getMapper(com.klei.goodfish.mapper.GoodMapper.class);
            Good good = goodMapper.findById(goodId);

            if (good == null || good.getStatus() == 0) {
                resp.setStatus(404);
                out.print(ResultUtil.fail(404, "商品不存在或已下架").toJson());
                return;
            }

            // 转换为 Map
            Map<String, Object> result = new HashMap<>();
            result.put("id", good.getId());
            result.put("goodName", good.getGoodName());
            result.put("goodImage", good.getGoodImage());
            result.put("goodPrice", good.getGoodPrice());
            result.put("description", good.getDescription());
            result.put("status", good.getStatus());
            result.put("sellerId", good.getSellerId());
            result.put("sellingStatus", good.getSellingStatus());

            if (good.getCreateTime() != null) {
                result.put("createTime", good.getCreateTime().format(formatter));
            } else {
                result.put("createTime", "");
            }

            // 查询卖家信息
            User seller = userMapper.findById(good.getSellerId());
            Map<String, Object> sellerInfo = new HashMap<>();
            if (seller != null) {
                sellerInfo.put("userId", seller.getId());
                sellerInfo.put("userName", seller.getUserName());
                sellerInfo.put("avatar", seller.getAvatar());
            } else {
                sellerInfo.put("userId", good.getSellerId());
                sellerInfo.put("userName", "未知用户");
                sellerInfo.put("avatar", "");
            }
            result.put("sellerInfo", sellerInfo);

            // ★★★ 关键新增：如果用户已登录，查询是否已关注该卖家 ★★★
            HttpSession session = req.getSession(false);
            if (session != null) {
                Integer currentUserId = (Integer) session.getAttribute("userId");
                if (currentUserId != null && seller != null) {
                    boolean isFollowing = followService.isFollowing(currentUserId, seller.getId());
                    sellerInfo.put("isFollowing", isFollowing);

                    // 同时返回当前用户ID，方便前端使用
                    result.put("currentUserId", currentUserId);
                }
            }

            out.print(ResultUtil.success("查询成功", result).toJson());

        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print(ResultUtil.fail(400, "商品ID格式错误").toJson());
        } catch (BusinessException e) {
            resp.setStatus(e.getCode());
            out.print(ResultUtil.fail(e.getCode(), e.getMessage()).toJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "系统错误：" + e.getMessage()).toJson());
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