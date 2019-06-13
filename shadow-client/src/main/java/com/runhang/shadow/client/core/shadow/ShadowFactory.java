package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.client.core.bean.ShadowBean;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;
import com.runhang.shadow.client.core.mqtt.TopicUtils;
import com.runhang.shadow.client.core.sync.push.ControlPush;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ShadowFactory
 * @Description 影子管理
 * @Date 2019/2/1 18:09
 * @author szh
 **/
@Component
public class ShadowFactory {

    private static Logger log = LoggerFactory.getLogger(ShadowFactory.class);
    private static Map<String, String> beanMap = new HashMap<>();

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
            Object shadow = shadowClass.newInstance();

            /** 2. bean注入 **/
            return injectShadow(shadow, topic);

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            log.error("injectShadow failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * @Description 注入影子到容器
     * @param data 管理对象
     * @param topic 订阅主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/5 14:14
     */
    public static boolean injectShadow(Object data, String topic) {
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
    public static boolean batchInjectShadow(Map<String, Object> dataMap) {
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
        return shadowBean.updateShadowByServer(current);
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
        Map<String, Object> desired = shadowBean.getDoc().getState().getDesired();
        // 检查是否修改
        if (null == desired || desired.isEmpty()) {
            return ReErrorCode.SHADOW_ATTR_NOT_MODIFIED;
        }
        long current = shadowBean.getDoc().getTimestamp();
        controlPush.push(topic, shadowBean.getDoc(), current);
        return null;
    }

}
