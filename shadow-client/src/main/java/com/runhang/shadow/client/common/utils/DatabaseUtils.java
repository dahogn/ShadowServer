package com.runhang.shadow.client.common.utils;

/**
 * @ClassName DatabaseUtils
 * @Description 数据库相关工具类
 * @Date 2019/6/11 19:08
 * @author szh
 **/
public class DatabaseUtils {

    // 数据库映射接口名
    private static final String REPOSITORY_NAME = "Repository";

    /**
     * @Description 生成数据库映射接口名
     * @param entityName 对应实体名
     * @return 接口名
     * @author szh
     * @Date 2019/6/12 10:11
     */
    public static String generateRepositoryName(String entityName) {
        return entityName + REPOSITORY_NAME;
    }

    /**
     * @Description 是否是数据库接口
     * @param className 类名
     * @return 接口
     * @author szh
     * @Date 2019/6/12 10:12
     */
    public static boolean isRepository(String className) {
        return className.endsWith(REPOSITORY_NAME);
    }

}
