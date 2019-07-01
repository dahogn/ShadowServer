package com.runhang.shadow.client.device.database;

import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.client.common.utils.DatabaseUtils;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @ClassName DatabaseOperation
 * @Description 数据库实体操作
 * @Date 2019/6/27 16:12
 * @author szh
 **/
@Slf4j
public class DatabaseOperation {

    /**
     * @Description 保存实体到数据库
     * @param entity 实体
     * @author szh
     * @Date 2019/6/27 22:26
     */
    public static void saveEntity(ShadowEntity entity) {
        // 获取repository名
        String repositoryName = DatabaseUtils.generateRepositoryName(entity.getClass().getSimpleName());
        if (Character.isUpperCase(repositoryName.charAt(0))) {
            repositoryName = Character.toLowerCase(repositoryName.charAt(0)) + repositoryName.substring(1);
        }
        try {
            // 从ioc容器中取出
            Object repository = BeanUtils.getBean(repositoryName);
            if (null != repository) {
                // 调用save方法
                Method saveMethod = repository.getClass().getMethod("save", Object.class);
                saveMethod.invoke(repository, entity);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
        }
    }

}
