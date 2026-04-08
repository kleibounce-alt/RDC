package com.klei.goodfish.util;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

/**
 * @author klei
 */
public class DBUtil {
    private static String url;
    private static String username;
    private static String password;

    // 静态块：类加载时执行一次，加载驱动
    static {
        try {
            // 读配置文件
            InputStream is = DBUtil.class.getClassLoader()
                    .getResourceAsStream("jdbc.properties");
            Properties prop = new Properties();
            prop.load(is);

            url = prop.getProperty("jdbc.url");
            username = prop.getProperty("jdbc.username");
            password = prop.getProperty("jdbc.password");
            String driver = prop.getProperty("jdbc.driver");

            // 注册驱动（MySQL 8 用 com.mysql.cj.jdbc.Driver）
            Class.forName(driver);
        } catch (Exception e) {
            throw new RuntimeException("数据库配置加载失败", e);
        }
    }

    // 获取连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    // 关闭资源（查操作用：关 ResultSet, Statement, Connection）
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) {rs.close();} } catch (Exception e) {}
        try { if (stmt != null) {stmt.close();} } catch (Exception e) {}
        try { if (conn != null) {conn.close();} } catch (Exception e) {}
    }

    // 关闭资源（增删改用：只关 Statement, Connection）
    public static void close(Statement stmt, Connection conn) {
        close(null, stmt, conn);
    }
}