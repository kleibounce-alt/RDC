package com.klei.goodfish.mappercore.proxy;

import com.klei.goodfish.mappercore.*;
import com.klei.goodfish.util.DBUtil;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * @author klei
 */
public class MapperProxy implements InvocationHandler {

    //让service能获取代理
    public static <T> T getMapper(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new MapperProxy()
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取方法上的各个注解
        Select select = method.getAnnotation(Select.class);
        Insert insert = method.getAnnotation(Insert.class);
        Update update = method.getAnnotation(Update.class);
        Delete delete = method.getAnnotation(Delete.class);

        // 获取 SQL 和判断操作类型
        String sql = null;

        int operationType = 0;
        if (select != null) {
            sql = select.value();
            operationType = 1;
        } else if (insert != null) {
            sql = insert.value();
            operationType = 2;
        } else if (update != null) {
            sql = update.value();
            operationType = 3;
        } else if (delete != null) {
            sql = delete.value();
            operationType = 4;
        } else {
            return method.invoke(this, args);
        }

        // 执行 JDBC
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    pstmt.setObject(i + 1, args[i]);
                }
            }

            // 根据操作类型执行不同逻辑
            if (operationType == 1) {
                //返回结果集
                rs = pstmt.executeQuery();
                return handleResultSet(rs, method);
            } else {

                return pstmt.executeUpdate();
            }

        } finally {
            // 统一关闭资源（Select 有 ResultSet，其他没有）
            if (operationType == 1) {
                DBUtil.close(rs, pstmt, conn);
            } else {
                DBUtil.close(null, pstmt, conn);
            }
        }
    }

    // 处理查询结果
    private Object handleResultSet(ResultSet rs, Method method) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
        }

        Class<?> returnType = method.getReturnType();

        // 处理返回 List<实体类> 的情况（如查询收藏列表）
        if (returnType == List.class) {
            // 获取 List 里的泛型类型（如 List<Good> 中的 Good）
            Type genericType = method.getGenericReturnType();
            Class<?> actualType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];

            // 把 List<Map> 转成 List<实体类>
            List<Object> result = new ArrayList<>();
            for (Map<String, Object> row : list) {
                Object obj = mapToObj(row, actualType);
                result.add(obj);
            }
            return result;
        }

        // 处理返回单个对象的情况（如查询个人信息）
        if (list.isEmpty()) {
            return null;
        }

        Map<String, Object> row = list.getFirst();
        if (returnType == Map.class) {
            return row;
        }

        return mapToObj(row, returnType);
    }

    // 提取的公共方法：Map 转对象
    private Object mapToObj(Map<String, Object> row, Class<?> clazz) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String fieldName = toCamelCase(entry.getKey());
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, entry.getValue());
            } catch (Exception e) {
                // 字段不存在跳过
            }
        }
        return obj;
    }

    //添加驼峰映射
    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {return str;}

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}