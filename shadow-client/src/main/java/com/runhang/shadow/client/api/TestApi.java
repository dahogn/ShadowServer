package com.runhang.shadow.client.api;

import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.device.entity.Vending;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName TestApi
 * @Description 测试接口
 * @Date 2019/7/1 11:41
 * @author szh
 **/
@Slf4j
@Controller
public class TestApi {

    @Autowired
    private MqttTopicFactory mqttTopicFactory;

    @RequestMapping("publish")
    public String publish(@RequestParam("topic") String topic,
                          @RequestBody String msg) {
        mqttTopicFactory.publishTypeOne(topic, msg);
        return "";
    }

    @RequestMapping("subscribe")
    public String subscribe(@RequestParam("topic") String topic) {
        mqttTopicFactory.subscribeTypeOne(topic);
        return "";
    }

    @RequestMapping("modify")
    public String modify() {
        Vending vending = (Vending) ShadowUtils.getShadow("vending");
        if (null != vending) {
            vending.setName("vending3");
            ReErrorCode error = ShadowUtils.commitAndPush("vending");
            if (null != error) {
                log.error(error.getErrorMsg());
            }
        }
        return "";
    }

}
