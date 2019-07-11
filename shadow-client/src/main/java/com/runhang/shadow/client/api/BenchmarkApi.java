package com.runhang.shadow.client.api;

import com.alibaba.fastjson.JSON;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;
import com.runhang.shadow.client.service.ShadowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BenchmarkApi
 * @Description 性能测试api
 * @Date 2019/7/10 11:43
 * @author szh
 **/
@Slf4j
@Controller
@RequestMapping("benchmark")
public class BenchmarkApi {

    @Autowired
    private ShadowService shadowService;
    @Autowired
    private MqttTopicFactory mqttTopicFactory;

    /**
     * @Description 数据库中增加
     * @author szh
     * @Date 2019/7/10 11:52
     */
    @PostMapping("device")
    public void addDevice(@RequestParam("number") int number) {
        shadowService.addDevice(number);
    }

    /**
     * @Description 影子方式更新售货机名称属性
     * @author szh
     * @Date 2019/7/11 11:01
     */
    @PatchMapping("vending")
    public void updateVending() {
        List<Map<String, String>> msgList = shadowService.findDevice();
        for (Map<String, String> msgMap : msgList) {
            shadowService.updatePublish(msgMap);
        }
    }

    /**
     * @Description 订阅设备端主题
     * @param number 订阅数量
     * @author szh
     * @Date 2019/7/11 11:00
     */
    @PostMapping("subscribe")
    public void subscribe(@RequestParam("number") int number) {
        for (int i = 1; i <= number; i++) {
            mqttTopicFactory.subscribeTypeOne("get/vending" + i);
        }
    }


    @PostMapping("db/vending")
    @ResponseBody
    public String updateVendingViaDB(@RequestParam("vendingId") int vendingId,
                                   @RequestParam("name") String name) {
        shadowService.updateVendingName(vendingId, name);

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("vendingId", String.valueOf(vendingId));
        returnMap.put("timestamp", "" + System.currentTimeMillis());
        return JSON.toJSONString(returnMap);
    }

    @PatchMapping("db/vending")
    public void updateVendingViaDBTest() {
        List<List<String>> deviceList = shadowService.findDeviceViaDB();
        for (List<String> urlList : deviceList) {
            shadowService.updateViaDB(urlList);
        }
    }

}
