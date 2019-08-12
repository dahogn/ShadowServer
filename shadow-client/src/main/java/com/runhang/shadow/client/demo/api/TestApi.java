package com.runhang.shadow.client.demo.api;

import com.alibaba.fastjson.JSON;
import com.runhang.shadow.client.common.utils.ClassUtils;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.device.entity.CargoRoad;
import com.runhang.shadow.client.device.entity.Commodity;
import com.runhang.shadow.client.device.entity.Vending;
import com.runhang.shadow.client.demo.service.ShadowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    private ShadowService shadowService;

    @RequestMapping("publish")
    public String publish(@RequestParam("topic") String topic,
                          @RequestBody String msg) {
        mqttTopicFactory.publishTypeZero(topic, msg);
        return "";
    }

    @RequestMapping("subscribe")
    public String subscribe(@RequestParam("topic") String topic) {
        mqttTopicFactory.subscribeTypeOne(topic);
        return "";
    }

    @RequestMapping("modify")
    public String modify() throws Exception {
        Vending vending = (Vending) ShadowUtils.getShadow("vending");
        if (null != vending) {
            //vending.setName("vending3");
            CargoRoad cargoRoad = new CargoRoad("vending");
            cargoRoad.setSerial(1);
            Commodity commodity = new Commodity("vending");
            commodity.setPrice(1.0);
            commodity.setNumber(10);
            commodity.setName("cake");
            List<Commodity> commodityList = new ArrayList<>();
            commodityList.add(commodity);
            cargoRoad.setCommodity(commodityList);
            vending.getCargoRoad().add(cargoRoad);
            ReErrorCode error = ShadowUtils.commitAndPush("vending");
            if (null != error) {
                log.error(error.getErrorMsg());
            }
        }
        return "";
    }

    @RequestMapping("entityList")
    @ResponseBody
    public String entityList() {
        List<String> names = ClassUtils.getAllEntityName();
        return JSON.toJSONString(names);
    }

    @RequestMapping("vending")
    public String getVending() {
        shadowService.getVending();
        return "";
    }

}
