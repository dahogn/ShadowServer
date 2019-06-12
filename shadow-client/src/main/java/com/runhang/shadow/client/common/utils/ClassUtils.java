package com.runhang.shadow.client.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @ClassName ClassUtils
 * @Description 类操作工具
 * @Date 2019/4/30 9:38
 * @author szh
 **/
public class ClassUtils {

    // getter & setter 选项
    public static final String METHOD_GETTER = "get";
    public static final String METHOD_SETTER = "set";

    /**
     * 获取getter或setter方法名称
     *
     * @param propertyName 属性名
     * @param method 方法类型：ClassGenerateUtils.METHOD_GETTER 或 ClassGenerateUtils.METHOD_SETTER
     * @return 方法名
     */
    public static String getGetterSetterName(String propertyName, String method) {
        String upperPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        return method + upperPropertyName;
    }

    /**
     * 设置bean的属性值
     *
     * @param obj       bean对象
     * @param fieldName 属性名
     * @param value     属性值
     * @return          是否成功
     */
    public static boolean setValue(Object obj, String fieldName, Object value) {
        Class clazz = obj.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取bean的属性值
     *
     * @param obj       bean对象
     * @param fieldName 属性名称
     * @return          属性值
     */
    public static Object getValue(Object obj, String fieldName) {
        Class clazz = obj.getClass();
        String methodName = getGetterSetterName(fieldName, METHOD_GETTER);
        Object returnValue;
        try {
            Method method = clazz.getMethod(methodName);
            returnValue = method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return returnValue;
    }

}
