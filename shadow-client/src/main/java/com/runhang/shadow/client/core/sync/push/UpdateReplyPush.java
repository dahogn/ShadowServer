package com.runhang.shadow.client.core.sync.push;

import com.runhang.shadow.client.core.bean.ReContent;
import com.runhang.shadow.client.core.bean.RePayload;
import com.runhang.shadow.client.core.bean.ShadowConst;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.mqtt.TopicUtils;

/**
 * @ClassName UpdateReplyPush
 * @Description 更新结果回复推送
 * @Date 2019/5/3 17:30
 * @author szh
 **/
public class UpdateReplyPush extends MessagePush {

    /**
     * @Description 更新成功后的推送
     * @param topic 推送主题
     * @param version 影子版本
     * @author szh
     * @Date 2019/5/3 17:52
     */
    public void pushSuccess(String topic, int version) {
        RePayload rePayload = new RePayload(ShadowConst.PAYLOAD_STATUS_SUCCESS, version);
        String publishTopic = TopicUtils.getGetTopic(topic);
        assembleAndPublish(ShadowConst.REPLY_METHOD_REPLY, rePayload,
                publishTopic, System.currentTimeMillis(), null);
    }

    /**
     * @Description 更新失败后的推送
     * @param topic 推送主题
     * @param errorCode 失败原因
     * @author szh
     * @Date 2019/5/3 18:35
     */
    public void pushError(String topic, ReErrorCode errorCode) {
        ReContent reContent = new ReContent(errorCode);
        RePayload rePayload = new RePayload(ShadowConst.PAYLOAD_STATUS_ERROR, reContent);
        String publishTopic = TopicUtils.getGetTopic(topic);
        assembleAndPublish(ShadowConst.REPLY_METHOD_REPLY, rePayload,
                publishTopic, System.currentTimeMillis(), null);
    }

}
