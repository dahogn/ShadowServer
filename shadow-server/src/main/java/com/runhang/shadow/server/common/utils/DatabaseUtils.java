package com.runhang.shadow.server.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName DatabaseUtils
 * @Description 数据库相关工具类
 * @Date 2019/6/11 19:08
 * @author szh
 **/
class DatabaseUtils {

    // 数据库映射接口名
    private static final String REPOSITORY_NAME = "Repository";

    /**
     * @Description 生成外键
     * @param field 类字段名
     * @return 外键名
     * @author szh
     * @Date 2019/6/11 19:28
     */
    static String generateForeignKey(String field) {
        String key = camel2Underline(new StringBuffer(field)).toString();
        if (key.startsWith("_")) {
            key = key.substring(1);
        }
        return key + "_id";
    }

    /**
     * @Description 驼峰转下划线
     * @param str 驼峰式命名
     * @return 下划线分隔命名
     * @author szh
     * @Date 2019/6/11 19:28
     */
    private static StringBuffer camel2Underline(StringBuffer str) {
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer(str);
        if (matcher.find()) {
            sb = new StringBuffer();
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
            matcher.appendTail(sb);
        } else {
            return sb;
        }
        return camel2Underline(sb);
    }

    /**
     * @Description 生成数据库映射接口名
     * @param entityName 对应实体名
     * @return 接口名
     * @author szh
     * @Date 2019/6/12 10:11
     */
    static String generateRepositoryName(String entityName) {
        return entityName + REPOSITORY_NAME;
    }

    /**
     * @Description 是否是数据库接口
     * @param className 类名
     * @return 接口
     * @author szh
     * @Date 2019/6/12 10:12
     */
    static boolean isRepository(String className) {
        return className.endsWith(REPOSITORY_NAME);
    }

}
