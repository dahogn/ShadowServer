package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.client.common.utils.ClassUtils;
import com.runhang.shadow.client.core.exception.NoSriException;
import com.runhang.shadow.client.core.exception.NoTopicException;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName EntityFactory
 * @Description 实体相关操作
 * @Date 2019/7/4 9:38
 * @author szh
 **/
public class EntityFactory {

    /**
     * 保存实体的SRI
     */
    private static Set<String> entitySriSet = new HashSet<>();

    /**
     * @Description 判断sri是否存在
     * @param sri 实体sri
     * @return 存在
     * @author szh
     * @Date 2019/7/2 15:05
     */
    public static boolean isSriExist(String sri) {
        return entitySriSet.contains(sri);
    }

    /**
     * @Description 注入影子的各个部分
     * @param shadowEntity 影子部分实体
     * @throws NoTopicException 实体无主题异常
     * @throws NoSriException 实体无sri异常
     * @return 是否成功
     * @author szh
     * @Date 2019/6/16 19:51
     */
    public static boolean injectEntity(ShadowEntity shadowEntity) throws NoTopicException, NoSriException {
        if (StringUtils.isEmpty(shadowEntity.getEntityTopic())) {
            throw new NoTopicException();
        }
        String sri = shadowEntity.getSRI();
        if (StringUtils.isEmpty(shadowEntity.getSRI())) {
            throw new NoSriException();
        }
        if (entitySriSet.contains(sri)) {
            return false;
        }
        entitySriSet.add(sri);
        BeanUtils.injectExistBean(shadowEntity, sri);
        // 增加订阅
        shadowEntity.addObserver(new EntityDataObserver(shadowEntity.getEntityTopic(), sri));
        return true;
    }

    /**
     * @Description 递归注入所有实体
     * @param shadowEntity 实体
     * @param topic 主题
     * @param entityNames 所有实体类名
     * @author szh
     * @Date 2019/6/18 11:23
     */
    public static void injectEntities(ShadowEntity shadowEntity, String topic,
                                      List<String> entityNames) throws NoTopicException, NoSriException {
        // 实体sri不合法就生成一个
        if (!shadowEntity.checkSRI()) {
            shadowEntity.generateSRI();
        }
        // 注入自身
        shadowEntity.setEntityTopic(topic);
        injectEntity(shadowEntity);
        // 获取所有属性类型
        Class entityClass = shadowEntity.getClass();
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldType = field.getType().getSimpleName();
            if (entityNames.contains(fieldType)) {
                // 如果是实体直接注入
                ShadowEntity shadowField = (ShadowEntity) ClassUtils.getValue(shadowEntity, field.getName());
                if (null != shadowField) {
                    injectEntities(shadowField, topic, entityNames);
                }
            } else if ("List".equals(fieldType)) {
                // 如果是list，遍历注入
                List<ShadowEntity> shadowFields = (List<ShadowEntity>) ClassUtils.getValue(shadowEntity, field.getName());
                if (null != shadowFields) {
                    for (ShadowEntity shadowField : shadowFields) {
                        injectEntities(shadowField, topic, entityNames);
                    }
                }
            }
        }
    }

    /**
     * @Description 清空实体
     * @author szh
     * @Date 2019/6/18 0:09
     */
    public static void destroyEntities() {
        for (String beanName : entitySriSet) {
            BeanUtils.destroyBean(beanName);
        }
        entitySriSet.clear();
    }

    /**
     * @Description 通过sri获取实体
     * @param sri sri
     * @return 影子实体
     * @author szh
     * @Date 2019/7/2 15:11
     */
    public static ShadowEntity getEntity(String sri) {
        if (!isSriExist(sri)) {
            return null;
        }
        return (ShadowEntity) BeanUtils.getBean(sri);
    }

}
