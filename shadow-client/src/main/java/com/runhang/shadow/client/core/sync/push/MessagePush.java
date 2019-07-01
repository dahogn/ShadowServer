package com.runhang.shadow.client.core.sync.push;

import com.runhang.shadow.client.core.bean.comm.RePayload;
import com.runhang.shadow.client.core.bean.comm.ShadowReplyBean;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;

/**
 * @ClassName MessagePush
 * @Description 消息回复
 * @Date 2019/5/3 18:30
 * @author szh
 **/
class MessagePush {

    private MqttTopicFactory mqttTopicFactory;

    public MessagePush() {
        this.mqttTopicFactory = new MqttTopicFactory();
    }

    /**
     * @Description 组织数据并推送
     * @param method 推送方法
     * @param rePayload 信息载荷
     * @param topic 推送主题
     * @param timestamp 信息时间戳
     * @param version 信息版本
     * @author szh
     * @Date 2019/5/3 21:16
     */
    void assembleAndPublish(String method, RePayload rePayload, String topic, long timestamp, Integer version) {
        ShadowReplyBean replyBean = new ShadowReplyBean();
        replyBean.setMethod(method);
        replyBean.setPayload(rePayload);
        replyBean.setTimestamp(timestamp);
        if (null != version) {
            replyBean.setVersion(version);
        }
        mqttTopicFactory.publishTypeOne(topic, replyBean.toString());
    }

}
