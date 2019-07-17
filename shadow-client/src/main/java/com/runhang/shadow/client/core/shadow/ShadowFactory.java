package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.client.core.bean.shadow.ShadowBean;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;
import com.runhang.shadow.client.core.mqtt.TopicUtils;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * @author szh
 * @ClassName ShadowFactory
 * @Description 影子管理
 * @Date 2019/2/1 18:09
 **/
@Slf4j
public class ShadowFactory {

    /** 保存影子topic与容器id关系 */
    private static Map<String, String> beanMap = new HashMap<>();
    /** 保存影子的信号量*/
    private static Map<String, Semaphore> semaphoreMap = new HashMap<>();
    /** 保存线程名称与对应的topics*/
    private static Map<String,List<String>> threadMap = new HashMap<>();

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
        // 标注信号量
        semaphoreMap.put(topic,new Semaphore(1));
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

    /**
     * @param data  管理对象
     * @param topic 订阅主题
     * @return 是否成功
     * @Description 注入影子到容器
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
     * @param dataMap 影子与主题
     * @return 是否成功
     * @Description 批量注入影子
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
     * 获取信号量
     * @param topic
     * @return
     */
    public static Semaphore getSemaphore(String topic){
        return semaphoreMap.get(topic);
    }

    public static List<String> getThreadTopic(String threadName){
        return threadMap.get(threadName);
    }

    public static Map<String,List<String>> getThreadMap(){
        return threadMap;
    }

}
