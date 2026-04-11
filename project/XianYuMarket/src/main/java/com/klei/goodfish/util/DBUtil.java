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

    // 线程本地存储，用于事务管理（关键修复）
    private static ThreadLocal<Connection> threadLocalConn = new ThreadLocal<>();

    static {
        try {
            InputStream is = DBUtil.class.getClassLoader()
                    .getResourceAsStream("jdbc.properties");
            Properties prop = new Properties();
            prop.load(is);

            url = prop.getProperty("jdbc.url");
            username = prop.getProperty("jdbc.username");
            password = prop.getProperty("jdbc.password");
            String driver = prop.getProperty("jdbc.driver");

            Class.forName(driver);
        } catch (Exception e) {
            throw new RuntimeException("数据库配置加载失败", e);
        }
    }

    // 获取连接（支持事务）
    public static Connection getConnection() throws SQLException {
        Connection conn = threadLocalConn.get();
        if (conn != null && !conn.isClosed()) {
            return conn;
        }
        return DriverManager.getConnection(url, username, password);
    }

    // 开启事务（关键修复）
    public static void beginTransaction() throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);
        threadLocalConn.set(conn);
    }

    // 提交事务
    public static void commit() throws SQLException {
        Connection conn = threadLocalConn.get();
        if (conn != null) {
            conn.commit();
            conn.close();
            threadLocalConn.remove();
        }
    }

    // 回滚事务
    public static void rollback() {
        Connection conn = threadLocalConn.get();
        if (conn != null) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                threadLocalConn.remove();
            }
        }
    }

    // 是否处于事务中
    public static boolean isInTransaction() {
        try {
            Connection conn = threadLocalConn.get();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) {rs.close();} } catch (Exception e) {}
        try { if (stmt != null) {stmt.close();} } catch (Exception e) {}
        if (!isInTransaction()) {
            try { if (conn != null) {conn.close();} } catch (Exception e) {}
        }
    }

    public static void close(Statement stmt, Connection conn) {
        close(null, stmt, conn);
    }
}