package com.klei.goodfish.mappercore.proxy;

import com.klei.goodfish.mappercore.*;
import com.klei.goodfish.util.DBUtil;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MapperProxy implements InvocationHandler {

    //让Service能获取代理
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
            // 统一关闭资源（Select 有 ResultSet，其它没有）
            if (operationType == 1) {
                DBUtil.close(rs, pstmt, conn);
            } else {
                DBUtil.close(null, pstmt, conn);
            }
        }
    }

    // 处理查询结果 - 关键修复：增加标量类型处理
    private Object handleResultSet(ResultSet rs, Method method) throws Exception {
        Class<?> returnType = method.getReturnType();

        // ===== 关键新增：处理返回值为标量类型的情况（如 int, long, String 等）=====
        if (isScalarType(returnType)) {
            if (rs.next()) {
                Object value = rs.getObject(1);
                return convertToScalar(value, returnType);
            }
            // 如果没有结果，返回默认值
            return getDefaultScalarValue(returnType);
        }

        // 原有逻辑：处理返回 List<Map> 或 单个对象/Map 的情况
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                // 关键修改：使用 getColumnLabel 获取 SQL 别名（as goodPrice），而非原始列名（price）
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }

        // 处理返回 List<实体> 的情况（如查询收藏列表）
        if (returnType == List.class) {
            // 获取 List 里的泛型类型（如 List<Good> 中的 Good）
            Type genericType = method.getGenericReturnType();
            Class<?> actualType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];

            // 如果泛型也是标量类型（如 List<Integer>），直接返回原始值列表
            if (isScalarType(actualType)) {
                List<Object> scalarList = new ArrayList<>();
                for (Map<String, Object> row : list) {
                    Object value = row.values().iterator().next(); // 获取第一列的值
                    scalarList.add(convertToScalar(value, actualType));
                }
                return scalarList;
            }

            // 将 List<Map> 转成 List<实体>
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

        // Java 8 兼容性，get(0) 替代 getFirst()
        Map<String, Object> row = list.get(0);
        if (returnType == Map.class) {
            return row;
        }

        return mapToObj(row, returnType);
    }

    // ===== 新增：判断是否为标量类型 =====
    private boolean isScalarType(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Short.class ||
                clazz == Byte.class ||
                clazz == Float.class ||
                clazz == Double.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == BigDecimal.class ||
                clazz == Date.class ||
                clazz == java.util.Date.class ||
                clazz == Timestamp.class ||
                clazz == LocalDate.class ||
                clazz == LocalDateTime.class;
    }

    // ===== 新增：将值转换为标量类型 =====
    private Object convertToScalar(Object value, Class<?> targetType) {
        if (value == null) {
            return getDefaultScalarValue(targetType);
        }

        // 如果类型已经匹配，直接返回
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // 处理基本类型和包装类的转换
        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
            return Integer.valueOf(value.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value instanceof String) {
                return Long.parseLong((String) value);
            }
            return Long.valueOf(value.toString());
        }
        if (targetType == short.class || targetType == Short.class) {
            if (value instanceof Number) {
                return ((Number) value).shortValue();
            }
            return Short.valueOf(value.toString());
        }
        if (targetType == byte.class || targetType == Byte.class) {
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            }
            return Byte.valueOf(value.toString());
        }
        if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return Float.valueOf(value.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.valueOf(value.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.valueOf(value.toString());
        }
        if (targetType == String.class) {
            return value.toString();
        }

        return value;
    }

    // ===== 新增：获取标量类型的默认值 =====
    private Object getDefaultScalarValue(Class<?> targetType) {
        if (targetType == int.class) {
            return 0;
        }
        if (targetType == long.class) {
            return 0L;
        }
        if (targetType == short.class) {
            return (short) 0;
        }
        if (targetType == byte.class) {
            return (byte) 0;
        }
        if (targetType == float.class) {
            return 0.0f;
        }
        if (targetType == double.class) {
            return 0.0d;
        }
        if (targetType == boolean.class) {
            return false;
        }
        if (targetType == char.class) {
            return '\u0000';
        }
        // 包装类和 String 返回 null
        return null;
    }

    // 提取的公共方法：Map 转对象（修复字段映射和类型转换）
    private Object mapToObj(Map<String, Object> row, Class<?> clazz) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            } // 跳过null值

            // 策略1：先尝试原始key（处理 SQL as 别名，如 goodPrice）
            Field field = null;
            try {
                field = clazz.getDeclaredField(key);
            } catch (NoSuchFieldException e) {
                // 策略2：尝试驼峰转换（处理下划线，如 good_price -> goodPrice）
                String camelKey = toCamelCase(key);
                try {
                    field = clazz.getDeclaredField(camelKey);
                } catch (NoSuchFieldException e2) {
                    // 策略3：尝试全小写匹配
                    try {
                        field = clazz.getDeclaredField(key.toLowerCase());
                    } catch (NoSuchFieldException e3) {
                        // 字段不存在，跳过
                        continue;
                    }
                }
            }

            if (field != null) {
                field.setAccessible(true);

                // 关键修复：处理类型转换（特别是SQL时间类型到Java8时间API）
                Object convertedValue = convertValueToFieldType(value, field.getType());
                if (convertedValue != null) {
                    try {
                        field.set(obj, convertedValue);
                    } catch (IllegalArgumentException e) {
                        // 如果转换后仍然无法设置，打印日志但不中断（避免500错误）
                        System.err.println("字段类型不匹配: " + field.getName() +
                                ", 期望类型: " + field.getType().getName() +
                                ", 实际类型: " + value.getClass().getName());
                    }
                }
            }
        }
        return obj;
    }

    // 关键修复：类型转换方法
    private Object convertValueToFieldType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // 如果类型已经匹配，直接返回
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // 处理 LocalDateTime（数据库返回通常是 Timestamp）
        if (targetType == LocalDateTime.class) {
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toLocalDateTime();
            }
            if (value instanceof java.sql.Date) {
                return ((java.sql.Date) value).toLocalDate().atStartOfDay();
            }
        }

        // 处理 LocalDate
        if (targetType == LocalDate.class) {
            if (value instanceof Date) {
                return ((Date) value).toLocalDate();
            }
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toLocalDateTime().toLocalDate();
            }
        }

        // 处理 BigDecimal 到基本数字类型的转换
        if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            if (targetType == int.class || targetType == Integer.class) {
                return bd.intValue();
            }
            if (targetType == long.class || targetType == Long.class) {
                return bd.longValue();
            }
            if (targetType == double.class || targetType == Double.class) {
                return bd.doubleValue();
            }
            if (targetType == float.class || targetType == Float.class) {
                return bd.floatValue();
            }
        }

        // 处理 Integer 到 Long 等（自动装箱拆箱的扩展）
        if (value instanceof Integer) {
            Integer i = (Integer) value;
            if (targetType == Long.class || targetType == long.class) {
                return i.longValue();
            }
        }

        // 如果无法转换，返回原值（让 field.set 自己决定是否抛出异常）
        return value;
    }

    // 添加驼峰映射（下划线转驼峰）
    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

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