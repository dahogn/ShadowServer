package com.runhang.shadow.client.core.sync.push;

import com.runhang.shadow.client.core.bean.comm.RePayload;
import com.runhang.shadow.client.core.bean.comm.ShadowConst;
import com.runhang.shadow.client.core.bean.shadow.ShadowDoc;
import com.runhang.shadow.client.core.mqtt.TopicUtils;

/**
 * @ClassName GetReplyPush
 * @Description 设备主动获取状态回复
 * @Date 2019/5/3 21:21
 * @author szh
 **/
public class GetReplyPush extends MessagePush {

    /**
     * @Description 推送属性
     * @param topic 主题
     * @param shadowDoc 影子文档
     * @param dataClass 影子对象类型
     * @author szh
     * @Date 2019/5/3 21:14
     */
    public void push(String topic, ShadowDoc shadowDoc, Class<?> dataClass) {
        RePayload rePayload = new RePayload(ShadowConst.PAYLOAD_STATUS_SUCCESS, shadowDoc.getAllStateTrans(dataClass), shadowDoc.getMetadata());
        String publishTopic = TopicUtils.getGetTopic(topic);
        assembleAndPublish(ShadowConst.REPLY_METHOD_REPLY, rePayload,
                publishTopic, System.currentTimeMillis(), shadowDoc.getVersion());
    }

}
