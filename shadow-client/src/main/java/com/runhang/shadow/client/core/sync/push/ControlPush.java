package com.runhang.shadow.client.core.sync.push;

import com.runhang.shadow.client.core.bean.comm.RePayload;
import com.runhang.shadow.client.core.bean.comm.ShadowConst;
import com.runhang.shadow.client.core.bean.shadow.ShadowDoc;
import com.runhang.shadow.client.core.mqtt.TopicUtils;

/**
 * @ClassName ControlPush
 * @Description 服务器下发属性推送
 * @Date 2019/5/3 20:49
 * @author szh
 **/
public class ControlPush extends MessagePush {

    /**
     * @Description 推送属性
     * @param topic 主题
     * @param shadowDoc 影子文档
     * @param timestamp 更新时间戳
     * @author szh
     * @Date 2019/5/3 21:14
     */
    public void push(String topic, ShadowDoc shadowDoc, long timestamp) {
        RePayload rePayload = new RePayload(ShadowConst.PAYLOAD_STATUS_SUCCESS, shadowDoc.getDesiredStateTrans(), shadowDoc.getMetadata());
        String publishTopic = TopicUtils.getGetTopic(topic);
        assembleAndPublish(ShadowConst.REPLY_METHOD_CONTROL, rePayload,
                publishTopic, timestamp, shadowDoc.getVersion());
    }

}
