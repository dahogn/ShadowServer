package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.client.common.utils.ClassUtils;
import com.runhang.shadow.client.core.bean.ShadowBean;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.exception.NoTopicException;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;
import com.runhang.shadow.client.core.mqtt.TopicUtils;
import com.runhang.shadow.client.core.sync.push.ControlPush;
import com.runhang.shadow.client.device.database.DatabaseOperation;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @ClassName ShadowFactory
 * @Description 影子管理
 * @Date 2019/2/1 18:09
 * @author szh
 **/
@Component
public class ShadowFactory {

    private static Logger log = LoggerFactory.getLogger(ShadowFactory.class);
    /**
     * 保存影子topic与容器id关系
     */
    private static Map<String, String> beanMap = new HashMap<>();
    /**
     * 保存实体的SRI
     */
    private static Set<String> entitySriSet = new HashSet<>();

    private static ControlPush controlPush = new ControlPush();

    /**
     * 注入影子到容器
     *
     * @param bean 要管理的影子
     * @return 是否成功
     * @author szh
     * @Date 2019/2/1 18:09
     */
    public static boolean injectShadow(ShadowBean bean) {
        /** 1. 检查bean是否存在 **/
        String topic = bean.getTopic();
        if (beanMap.containsKey(topic)) {
            return false;
        }
        /** 2. bean注入 **/
        String beanName = bean.getData().getClass().getSimpleName() + "_" + topic;
        beanMap.put(topic, beanName);
        BeanUtils.injectExistBean(bean, beanName);
        /** 3. mqtt订阅 **/
        MqttTopicFactory mqttTopicFactory = new MqttTopicFactory();
        mqttTopicFactory.subscribeTypeOne(TopicUtils.getUpdateTopic(topic));
        return true;
    }

    /***
     * 注入影子到容器
     *
     * @param className 设备类名
     * @param topic mqtt订阅主题
     * @return 是否成功
     * @author szh
     * @date 2019/2/8 21:53
     */
    public static boolean injectShadow(String className, String topic) {
        // 检查bean是否存在
        if (beanMap.containsKey(topic)) {
            return false;
        }
        try {

            /** 1. 实例化device对象 **/
            Class shadowClass = Class.forName(className);
            ShadowEntity shadow = (ShadowEntity) shadowClass.newInstance();

            /** 2. bean注入 **/
            return injectShadow(shadow, topic);

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            log.error("injectShadow failed: " + e.getMessage());
            return false;
        }
    }

    /* ================================================== 影子注入 ================================================== */

    /**
     * @Description 注入影子到容器
     * @param data 管理对象
     * @param topic 订阅主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/5 14:14
     */
    public static boolean injectShadow(ShadowEntity data, String topic) {
        // 检查bean是否存在
        if (beanMap.containsKey(topic)) {
            return false;
        }
        ShadowBean shadowBean = new ShadowBean();
        shadowBean.setData(data);
        shadowBean.setTopic(topic);

        return injectShadow(shadowBean);
    }

    /**
     * @Description 批量注入影子
     * @param dataMap 影子与主题
     * @return 是否成功
     * @author szh
     * @Date 2019/6/13 14:45
     */
    public static boolean batchInjectShadow(Map<String, ShadowEntity> dataMap) {
        for (String topic : dataMap.keySet()) {
            boolean success = injectShadow(dataMap.get(topic), topic);
            if (!success) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过主题获取相应的影子
     *
     * @param topic 主题名称
     * @return 影子
     * @author szh
     * @date 2019/2/1 18:44
     */
    public static ShadowBean getShadowBean(String topic) {
        // TODO 通过索引和类名两种方式检索
        return (ShadowBean) BeanUtils.getBean(beanMap.get(topic));
    }

    /**
     * @Description 获取影子对象
     * @param topic 主题
     * @return 对象
     * @author szh
     * @Date 2019/5/2 20:46
     */
    public static Object getShadow(String topic) {
        ShadowBean shadowBean = getShadowBean(topic);
        if (null != shadowBean) {
            return shadowBean.getData();
        } else {
            return null;
        }
    }

    /* ================================================== 实体注入 ================================================== */

    /**
     * @Description 注入影子的各个部分
     * @param shadowEntity 影子部分实体
     * @throws NoTopicException 实体无主题异常
     * @return 是否成功
     * @author szh
     * @Date 2019/6/16 19:51
     */
    public static boolean injectEntity(ShadowEntity shadowEntity) throws NoTopicException {
        if (null == shadowEntity.getEntityTopic() || "".equals(shadowEntity.getEntityTopic())) {
            throw new NoTopicException();
        }
        String sri = shadowEntity.getSRI();
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
                                      List<String> entityNames) throws NoTopicException {
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

    /* ================================================== 影子通信 ================================================== */

    /**
     * @Description 通过主题更新影子文档
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/9 10:46
     */
    public static ReErrorCode commit(String topic) {
        ShadowBean shadowBean = getShadowBean(topic);
        long current = System.currentTimeMillis();
        ReErrorCode errorCode = shadowBean.updateShadowByServer(current);
        if (null == errorCode) {
            // 保存到数据库
            DatabaseOperation.saveEntity(shadowBean.getData());
        }
        return errorCode;
    }

    /**
     * @Description 通过主题更新影子文档并下发
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/2 16:17
     */
    public static ReErrorCode commitAndPush(String topic) {
        ShadowBean shadowBean = getShadowBean(topic);
        long current = System.currentTimeMillis();
        ReErrorCode error = shadowBean.updateShadowByServer(current);
        if (null == error) {
            // 下发状态
            controlPush.push(topic, shadowBean.getDoc(), current);
        }

        return error;
    }

    /**
     * @Description 下发影子修改
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/9 10:44
     */
    public static ReErrorCode push(String topic) {
        ShadowBean shadowBean = getShadowBean(topic);
        // 检查是否修改
        if (shadowBean.getDoc().getState().getDesired().isEmpty()) {
            return ReErrorCode.SHADOW_ATTR_NOT_MODIFIED;
        }
        long current = shadowBean.getDoc().getTimestamp();
        controlPush.push(topic, shadowBean.getDoc(), current);
        return null;
    }

}
