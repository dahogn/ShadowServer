package com.runhang.shadow.client.core.mqtt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.runhang.shadow.client.core.bean.comm.ShadowConst;
import com.runhang.shadow.client.core.bean.comm.ShadowOpsBean;
import com.runhang.shadow.client.core.sync.callback.ShadowDeleteCallback;
import com.runhang.shadow.client.core.sync.callback.ShadowGetCallback;
import com.runhang.shadow.client.core.sync.callback.ShadowUpdateCallback;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @ClassName PushCallback
 * @Description mqtt推送回调类
 * @Date 2019/2/2 21:45
 * @Author szh
 **/
@Slf4j
public class PushCallback implements MqttCallbackExtended {

    private ShadowDeleteCallback deleteCallback;
    private ShadowGetCallback getCallback;
    private ShadowUpdateCallback updateCallback;

    public PushCallback() {
        this.deleteCallback = new ShadowDeleteCallback();
        this.getCallback = new ShadowGetCallback();
        this.updateCallback = new ShadowUpdateCallback();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("连接断开，可以做重连");
        throwable.printStackTrace();
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        String msgContent = new String(mqttMessage.getPayload());
        log.info("接收消息主题 : " + s);
        log.info("接收消息Qos : " + mqttMessage.getQos());
        log.info("接收消息内容 : " + msgContent);

        try {
            // 配置关闭BigDecimal，默认状态下FastJson会把小数转换成BigDecimal型
            int disableDecimalFeature = JSON.DEFAULT_PARSER_FEATURE & ~Feature.UseBigDecimal.getMask();
            ShadowOpsBean opsBean = JSONObject.parseObject(msgContent, ShadowOpsBean.class, disableDecimalFeature);

            // 根据要求选择回调方法
            switch (opsBean.getMethod()) {
                case ShadowConst.OPERATION_METHOD_DELETE:
                    deleteCallback.run(s, opsBean);
                    break;
                case ShadowConst.OPERATION_METHOD_GET:
                    getCallback.run(s, opsBean);
                    break;
                case ShadowConst.OPERATION_METHOD_UPDATE:
                    updateCallback.run(s, opsBean);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("mqtt回调失败：" + e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("deliveryComplete---------" + iMqttDeliveryToken.isComplete());
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

    }
}
