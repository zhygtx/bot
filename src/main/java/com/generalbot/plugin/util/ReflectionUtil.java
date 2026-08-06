package com.generalbot.plugin.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * 反射工具类
 */
public class ReflectionUtil {

    /**
     * 解析对象并返回属性映射表
     *
     * @param obj 待解析的对象
     * @return 属性映射表
     */
    public static Map<String, Object> getAttributeMapWithType(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> attributeMap = new HashMap<>();
        Class<?> clazz = obj.getClass();

        // 递归处理当前类及其父类的字段
        processFields(clazz, attributeMap);

        return attributeMap;
    }

    /**
     * 递归处理类及其父类的字段
     */
    private static void processFields(Class<?> clazz, Map<String, Object> attributeMap) {
        // 遍历当前类的所有字段
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // 允许访问私有字段
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            // 如果字段已存在（子类覆盖父类字段），跳过
            if (attributeMap.containsKey(fieldName)) {
                continue;
            }

            // 处理基本类型和包装类
            if (isPrimitiveOrWrapper(fieldType)) {
                attributeMap.put(fieldName, fieldType.getSimpleName());
            }
            // 处理集合类型
            else if (Collection.class.isAssignableFrom(fieldType)) {
                attributeMap.put(fieldName, handleCollectionType(field));
            }
            // 处理 Map 类型
            else if (Map.class.isAssignableFrom(fieldType)) {
                attributeMap.put(fieldName, handleMapType(field));
            }
            // 处理自定义类（递归）
            else {
                // 判断是否为自定义类
                String typeName = isCustomClass(fieldType) ? fieldType.getName() : fieldType.getSimpleName();
                attributeMap.put(fieldName, typeName);
            }
        }

        // 递归处理父类字段
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            processFields(superClass, attributeMap);
        }
    }

    /**
     * 判断是否为自定义类
     */
    private static boolean isCustomClass(Class<?> clazz) {
        String packageName = clazz.getPackageName();
        // 系统包前缀列表
        return !(packageName.startsWith("java.") || packageName.startsWith("javax."));
    }

    /**
     * 处理集合类型的字段类型
     */
    private static String handleCollectionType(Field field) {
        Type genericType = field.getGenericType();
        Class<?> fieldType = field.getType();

        // 判断具体集合类型
        String collectionType;
        if (fieldType == List.class) {
            collectionType = "List";
        } else if (fieldType == Set.class) {
            collectionType = "Set";
        } else {
            collectionType = "Collection"; // 默认情况
        }

        // 处理泛型参数
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            String typeName = getTypeName(actualType);
            return collectionType + "<" + typeName + ">";
        }
        return collectionType;
    }

    /**
     * 处理 Map 类型的字段类型
     */
    private static String handleMapType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type keyType = parameterizedType.getActualTypeArguments()[0];
            Type valueType = parameterizedType.getActualTypeArguments()[1];
            String keyTypeName = getTypeName(keyType);
            String valueTypeName = getTypeName(valueType);
            return "Map<" + keyTypeName + ", " + valueTypeName + ">";
        }
        return "Map";
    }

    /**
     * 获取字段类型名称
     */
    private static String getTypeName(Type type) {
        if (type instanceof Class<?> clazz) {
            // 判断是否为自定义类
            return isCustomClass(clazz) ? clazz.getName() : clazz.getSimpleName();
        } else if (type instanceof ParameterizedType parameterizedType) {
            // 处理泛型类型
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            String rawTypeName = isCustomClass(rawType) ? rawType.getName() : rawType.getSimpleName();

            // 递归处理泛型参数
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            StringBuilder genericPart = new StringBuilder("<");
            for (int i = 0; i < actualTypes.length; i++) {
                genericPart.append(getTypeName(actualTypes[i]));
                if (i < actualTypes.length - 1) {
                    genericPart.append(", ");
                }
            }
            genericPart.append(">");

            return rawTypeName + genericPart;
        }
        // 处理其他类型（如通配符、数组等）
        return type.getTypeName();
    }

    /**
     * 判断是否为基本类型或包装类
     */
    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || type == String.class ||
                type == Integer.class || type == Double.class ||
                type == Boolean.class || type == Long.class ||
                type == Float.class || type == Short.class ||
                type == Byte.class || type == Character.class;
    }
}
