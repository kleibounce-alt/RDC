package com.klei.goodfish.controller;

import com.klei.goodfish.util.ResultUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet("/upload/image")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1MB
        maxFileSize = 1024 * 1024 * 5,        // 5MB
        maxRequestSize = 1024 * 1024 * 10       // 10MB
)
public class ImageUploadServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 检查登录（使用你设置的 userId）
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                resp.setStatus(401);
                out.print(ResultUtil.fail(401, "请先登录").toJson());
                return;
            }

            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "请选择图片文件").toJson());
                return;
            }

            // 检查文件类型
            String contentType = filePart.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                resp.setStatus(400);
                out.print(ResultUtil.fail(400, "只能上传图片文件").toJson());
                return;
            }

            // 创建上传目录
            String appPath = req.getServletContext().getRealPath("");
            String uploadPath = appPath + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String extension = contentType.contains("png") ? ".png" : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = uploadPath + File.separator + fileName;

            // 保存文件
            filePart.write(filePath);

            // 返回相对路径
            String relativePath = "/" + UPLOAD_DIR + "/" + fileName;
            Map<String, Object> data = new HashMap<>();
            data.put("url", relativePath);

            out.print(ResultUtil.success("上传成功", data).toJson());

        } catch (Exception e) {
            resp.setStatus(500);
            out.print(ResultUtil.fail(500, "上传失败：" + e.getMessage()).toJson());
        }
    }
}