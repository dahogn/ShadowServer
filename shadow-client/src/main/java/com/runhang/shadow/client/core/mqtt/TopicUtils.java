package com.runhang.shadow.client.core.mqtt;
/**
 * @ClassName TopicUtils
 * @Description 主题处理工具
 * @Date 2019/5/8 14:57
 * @author szh
 **/
public class TopicUtils {

    // MQTT主题
    private static final String MQTT_TOPIC_UPDATE = "update/";
    private static final String MQTT_TOPIC_GET = "get/";

    /**
     * @Description 获取更新主题
     * @param shadowId 影子标识符
     * @return mqtt订阅主题
     * @author szh
     * @Date 2019/5/8 15:04
     */
    public static String getUpdateTopic(String shadowId) {
        return MQTT_TOPIC_UPDATE + shadowId;
    }

    /**
     * @Description 获取获取主题
     * @param shadowId 影子标识符
     * @return mqtt订阅主题
     * @author szh
     * @Date 2019/5/8 15:05
     */
    public static String getGetTopic(String shadowId) {
        return MQTT_TOPIC_GET + shadowId;
    }

    /**
     * @Description 获取影子标识符
     * @param topic mqtt主题
     * @return 标识符
     * @author szh
     * @Date 2019/5/8 15:09
     */
    public static String getShadowId(String topic) {
        return topic.replaceFirst(MQTT_TOPIC_UPDATE, "");
    }

}
