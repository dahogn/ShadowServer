package com.runhang.shadow.client.core.sync.callback;

import com.runhang.shadow.client.core.bean.shadow.ShadowBean;
import com.runhang.shadow.client.core.bean.comm.ShadowOpsBean;
import com.runhang.shadow.client.core.mqtt.TopicUtils;
import com.runhang.shadow.client.core.shadow.ShadowFactory;

/**
 * @ClassName AbsMqttCallback
 * @Description mqtt回调抽象类
 * @Date 2019/4/29 11:11
 * @author szh
 **/
public abstract class AbsMqttCallback {

    /**
     * @Description 回调方法
     * @param topic 订阅的主题
     * @param opsBean 返回的消息
     * @author szh
     * @Date 2019/4/29 16:28
     */
    public void run(String topic, ShadowOpsBean opsBean) {
        // 取出容器中的对象
        String shadowId = TopicUtils.getShadowId(topic);
        ShadowBean shadowBean = ShadowFactory.getShadowBean(shadowId);
        // 处理逻辑
        dealMessage(opsBean, shadowBean);
    }

    /**
     * @Description ShadowOpsBean
     * @param opsBean 返回的消息
     * @param shadowBean 内存中的影子
     * @author szh
     * @Date 2019/4/29 16:37
     */
    public abstract void dealMessage(ShadowOpsBean opsBean, ShadowBean shadowBean);

}
