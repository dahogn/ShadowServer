package com.runhang.shadow.server.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName DatabaseUtils
 * @Description 数据库相关工具类
 * @Date 2019/6/11 19:08
 * @author szh
 **/
public class DatabaseUtils {

    /**
     * @Description 生成外键
     * @param field 类字段名
     * @return 外键名
     * @author szh
     * @Date 2019/6/11 19:28
     */
    public static String generateForginKey(String field) {
        return camel2Underline(new StringBuffer(field)) + "_id";
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

}
