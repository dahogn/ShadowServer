package com.runhang.shadow.client.service;

import com.alibaba.fastjson.JSONObject;
import com.runhang.shadow.client.core.mqtt.MqttTopicFactory;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.device.entity.CargoRoad;
import com.runhang.shadow.client.device.entity.Commodity;
import com.runhang.shadow.client.device.entity.Vending;
import com.runhang.shadow.client.device.repository.VendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ShadowService
 * @Description 影子服务
 * @Date 2019/7/10 11:50
 * @author szh
 **/
@Slf4j
@Service
public class ShadowService {

    @Autowired
    private VendingRepository vendingRepository;
    @Autowired
    private MqttTopicFactory mqttTopicFactory;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * @Description 数据库中插入设备
     * @param number 数量
     * @author szh
     * @Date 2019/7/11 11:03
     */
    @Transactional
    public void addDevice(int number) {
        for (int i = 0; i < number; i++) {
            Vending vending = new Vending();
            vending.generateSRI();
            vending.setName("vending" + i);
            vending.setTopic("vending" + i);

            CargoRoad cargoRoad = new CargoRoad();
            cargoRoad.generateSRI();
            cargoRoad.setSerial(i);
            List<CargoRoad> cargoRoadList = new ArrayList<>();
            cargoRoadList.add(cargoRoad);
            vending.setCargoRoad(cargoRoadList);

            Commodity commodity = new Commodity();
            commodity.generateSRI();
            commodity.setName("可乐" + i);
            commodity.setNumber(i);
            commodity.setPrice(i + 0.1);
            List<Commodity> commodityList = new ArrayList<>();
            commodityList.add(commodity);
            cargoRoad.setCommodity(commodityList);

            vendingRepository.save(vending);
        }
    }

    /**
     * @Description 更新售货机名
     * @param vendingId 售货机id
     * @param vendingName 售货机名
     * @author szh
     * @Date 2019/7/11 11:08
     */
    @Transactional
    @Async("asyncServiceExecutor")
    public void updateVendingName(int vendingId, String vendingName) {
        vendingRepository.updateName(vendingId, vendingName);
    }

    /**
     * @Description 查询设备并组织更新信息
     * @return 分组更新信息
     * @author szh
     * @Date 2019/7/11 11:04
     */
    public List<Map<String, String>> findDevice() {
        List<Vending> vendingList = vendingRepository.findAll();
        log.warn("start time: " + System.currentTimeMillis());
        List<Map<String, String>> msgList = new ArrayList<>();

        Map<String, String> msgMap = new HashMap<>();

        for (int i = 0; i < vendingList.size(); i++) {
            Vending vending = vendingList.get(i);
            String name = "new_" + vending.getName();
            String msg = String.format(
                    "{\"method\":\"update\",\"state\":{\"reported\":{\"update\":[{\"className\":\"Vending\"," +
                            "\"sri\":\"%s\",\"parentSri\":\"%s\"," +
                            "\"field\":{\"name\":\"%s\"}}],\"delete\":[],\"add\":[]},\"desired\":null},\"version\":2}",
                    vending.getSRI(), vending.getSRI(), name);
            msgMap.put("update/" + vending.getTopic(), msg);

            if (0 != i && i % 1000 == 0) {
                msgList.add(msgMap);
                msgMap = new HashMap<>();
            }
        }

        return msgList;
    }

    /**
     * @Description 多线程处理推送
     * @param msg 推送信息
     * @author szh
     * @Date 2019/7/11 11:05
     */
    public void updatePublish(Map<String, String> msg) {
        new UpdatePublishThread(msg).start();
    }

    /**
     * @Description 查询设备并组织数据库方式更新信息
     * @return url列表
     * @author szh
     * @Date 2019/7/12 9:19
     */
    public List<List<String>> findDeviceViaDB() {
        List<Vending> vendingList = vendingRepository.findAll();
        log.warn("start time: " + System.currentTimeMillis());

        List<List<String>> deviceList = new ArrayList<>();
        List<String> urlList = new ArrayList<>();

        for (int i = 0; i < 8121; i++) {
            Vending vending = vendingList.get(i);
            String name = "new_" + vending.getName();
            String url = String.format("http://192.168.0.106:8090/benchmark/db/vending?vendingId=%s&name=%s", i + 1, name);
            urlList.add(url);

            if (0 != i && i % 1000 == 0) {
                deviceList.add(urlList);
                urlList = new ArrayList<>();
            }
        }

        return deviceList;
    }

    /**
     * @Description 多线程数据库方式更新
     * @param urlList 访问的url
     * @author szh
     * @Date 2019/7/12 9:18
     */
    public void updateViaDB(List<String> urlList) {
        new UpdateViaDBThread(urlList).start();
    }

    /**
     * @Description 访问接口线程
     * @author szh
     * @Date 2019/7/12 9:19
     */
    private class UpdateViaDBThread extends Thread {

        List<String> urlList;

        UpdateViaDBThread(List<String> urlList) {
            this.urlList = urlList;
        }

        @Override
        public void run() {
            Map<String, Object> requestParam = new HashMap<>();

            for (String url : urlList) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestParam, headers);

                ResponseEntity<String> entity = restTemplate.postForEntity(url, request, String.class);
                String body = entity.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);

                log.info("修改vending id：" + jsonObject.get("vendingId"));
                log.info("返回时间：" + System.currentTimeMillis());
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

    }

    /**
     * @Description 推送线程
     * @author szh
     * @Date 2019/7/11 11:05
     */
    private class UpdatePublishThread extends Thread {
        Map<String, String> msg;

        UpdatePublishThread(Map<String, String> msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            for (String topic : msg.keySet()) {
                mqttTopicFactory.publishTypeZero(topic, msg.get(topic));
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    @com.runhang.shadow.client.core.aspect.ShadowService
    public void getVending() {
        Vending vending = (Vending) ShadowUtils.getShadow("vending");
        log.info("service thread: " + Thread.currentThread().getName());
    }

}
