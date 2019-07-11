package com.runhang.shadow.client.core.mqtt;

import com.runhang.shadow.client.common.config.MqttConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MqttTopicFactory
 * @Author wangzhaosen@runhangtech.com
 * @Date 2019/1/18 13:51
 * @Version 1.0
 * @Description 主题工厂类。
 **/
@Slf4j
@Service
public class MqttTopicFactory {

    private MqttConnectOptions mqttConnectOptions;

    private static final int DATA_QOS_ZERO = 0;
    private static final int DATA_QOS_ONE = 1;
    private static final int DATA_QOS_TWO = 2;

    private static MqttConfig mqttConfig;

    @Autowired
    public void setMqttConfig(MqttConfig mqttConfig) {
        MqttTopicFactory.mqttConfig = mqttConfig;
    }

    private static MqttClient mqttClient;

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2018/12/19 11:04
     * @Param []
     * @Description 新建MQTT客户端。
     **/
    private void createMqttClient() {
        try {
            String serverUrl = mqttConfig.getPreFix() + mqttConfig.getHost() + ":" + mqttConfig.getPort();
            //String createClientId = System.currentTimeMillis() + "" + Math.random();
            String createClientId = "shadow_server";
            MqttClient client = new MqttClient(serverUrl, createClientId, new MemoryPersistence());
            log.debug("---------------------create-mqtt:" + mqttConfig.getHost() + "---------------------------" + createClientId);
            mqttConnectOptions = new MqttConnectOptions();
            //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(mqttConfig.getUsername());
            mqttConnectOptions.setPassword(mqttConfig.getPassword().toCharArray());
            //设置超时时间，单位为秒
            mqttConnectOptions.setConnectionTimeout(mqttConfig.getTimeOut());
            //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
            mqttConnectOptions.setKeepAliveInterval(mqttConfig.getKeepAlive());
            //断开后，是否自动连接
            mqttConnectOptions.setAutomaticReconnect(true);
            //允许同时发送几条消息（未收到broker确认信息）
            mqttConnectOptions.setMaxInflight(1000);
            client.connect(mqttConnectOptions);
            client.setCallback(new PushCallback());
            mqttClient = client;
        } catch (Exception e) {
            log.error("创建MqttClient失败:" + e.getMessage());
        }
    }


    /**
     * @return java.util.Map<java.lang.String   ,   java.lang.Boolean>
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 0018 20:35
     * @Param [topicList]
     * @Description 获取所有Client的连接状态。
     **/
    public synchronized Map<String, Boolean> getAllClientConnectStatus(List<String> topicList) {
        Map<String, Boolean> dataMap = new HashMap<>();
        for (String topic : topicList) {
            dataMap.put(topic, isConnect());
        }
        return dataMap;
    }


    /**
     * @return boolean
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 0018 20:31
     * @Param [topic]
     * @Description 校验是否连接。
     **/
    public synchronized Boolean isConnect() {
        if (mqttClient == null) {
            return Boolean.FALSE;
        }
        return mqttClient.isConnected();
    }


    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 0018 20:28
     * @Param [topic]
     * @Description 根据主题做连接操作。
     **/
    public synchronized void connect(String topic) {
        if (mqttClient == null) {
            createMqttClient();
        }
        connect();
    }


    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 0018 16:35
     * @Param [mqttClient]
     * @Description 连接MQTT。
     **/
    private void connect() {
        mqttConnectOptions = new MqttConnectOptions();
        //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(mqttConfig.getUsername());
        mqttConnectOptions.setPassword(mqttConfig.getPassword().toCharArray());
        //设置超时时间，单位为秒
        mqttConnectOptions.setConnectionTimeout(mqttConfig.getTimeOut());
        //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        mqttConnectOptions.setKeepAliveInterval(mqttConfig.getKeepAlive());
        //断开后，是否自动连接
        mqttConnectOptions.setAutomaticReconnect(true);
        //允许同时发送几条消息（未收到broker确认信息）
        mqttConnectOptions.setMaxInflight(1000);
        //设置版本号
        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        try {
            mqttClient.setCallback(new PushCallback());
            mqttClient.connect(mqttConnectOptions);
        } catch (MqttException e) {
            log.error("创建MqttClient失败:" + e.getMessage());
        }
    }


    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 20:13
     * @Param [mqttClient]
     * @Description 默认连接方式。
     **/
    private void defaultConnect(MqttClient mqttClient) {
        try {
            mqttClient.connect();
        } catch (MqttException e) {
            log.error("创建MqttClient失败:" + e.getMessage());
        }
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2018/12/16 10:43
     * @Param [topic, pushMessage]
     * @Description 发布主题数据。
     **/
    public void publishTypeZero(String topic, String pushMessage) {
        publish(DATA_QOS_ZERO, false, topic, pushMessage);
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2018/12/16 10:43
     * @Param [topic, pushMessage]
     * @Description 发布主题数据。
     **/
    public void publishTypeZero(String topic, byte[] pushMessage) {
        publish(DATA_QOS_ZERO, true, topic, Arrays.toString(pushMessage));
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2018/12/16 10:43
     * @Param [topic, pushMessage]
     * @Description 发布主题数据。
     **/
    public void publishTypeOne(String topic, String pushMessage) {
        publish(DATA_QOS_ZERO, false, topic, pushMessage);
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2018/12/16 10:43
     * @Param [topic, pushMessage]
     * @Description 发布主题数据。
     **/
    public void publishTypeOne(String topic, byte[] pushMessage) {
        publish(DATA_QOS_ZERO, true, topic, Arrays.toString(pushMessage));
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2018/12/16 10:43
     * @Param [topic, pushMessage]
     * @Description 发布主题数据。
     **/
    public synchronized void publishTypeTwo(String topic, String pushMessage) {
        publish(DATA_QOS_TWO, false, topic, pushMessage);
    }

    /**
     * MQTT发布数据异常
     *
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2018/12/16 10:43
     * @Param [topic, pushMessage]
     * @Description 发布主题数据。
     **/
    public void publishTypeTwo(String topic, byte[] pushMessage) {
        publish(DATA_QOS_TWO, true, topic, Arrays.toString(pushMessage));
    }


    /**
     * 发布接口
     *
     * @param qos：订阅发送数据方式。
     * @param retained：是否保留消息-1：保留；0：不保留。 Broker会存储每个Topic的最后一条保留消息及其Qos，当订阅该Topic的客户端上线后，Broker需要将该消息投递给它。
     *                                    保留消息作用：可以让新订阅的客户端得到发布方的最新的状态值，而不必要等待发送。
     * @param topic：发布消息主题。
     * @param pushMessage：发布消息内容。
     */
    public void publish(int qos, boolean retained, String topic, String pushMessage) {
        /**1.client是否存在，不存在的话，新建一个*/
        if (mqttClient == null) {
            createMqttClient();
        }

        /**2.client是否已经连接中，没有连接则重新进行连接*/
        if (!mqttClient.isConnected()) {
            connect();
        }
        /**3.准备推送数据*/
        new PushMessageThread(mqttClient, qos, retained, topic, pushMessage).start();

    }

    class PushMessageThread extends Thread {

        private MqttClient mqttClient;
        private int qos;
        private boolean retained;
        private String topic;
        private String pushMessage;

        public PushMessageThread(MqttClient mqttClient, int qos, boolean retained, String topic, String pushMessage) {
            this.mqttClient = mqttClient;
            this.qos = qos;
            this.retained = retained;
            this.topic = topic;
            this.pushMessage = pushMessage;
        }

        @Override
        public void run() {
            super.run();
            MqttMessage message = new MqttMessage();
            message.setQos(qos);
            message.setRetained(retained);
            message.setPayload(pushMessage.getBytes());
            MqttTopic mTopic = mqttClient.getTopic(topic);
            if (null == mTopic) {
                log.error("未订阅该主题，先订阅主题");
                subscribe(topic, qos);
                mTopic = mqttClient.getTopic(topic);
            }
            /**4.推送数据*/
            MqttDeliveryToken token;
            try {
                token = mTopic.publish(message);
                log.info("With delivery token \"" + token.hashCode()
                        + " delivered: " + token.isComplete());
                token.waitForCompletion();
            } catch (Exception e) {
                log.error("MQTT发布数据异常，主题：'" + topic + "'内容：‘" + message + "’" + "出错信息：" + e.getMessage());
            }
        }
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 0018 19:39
     * @Param [topic]
     * @Description 订阅某个主题，qos为0：至多1次
     * 说明：零是最低的级别，但它具有最高的传输性能。接收者不会应答消息，发送者也不会保存和重发消息。
     * 这种模式常被称作“发射后不管”，它提供和TCP协议一致的可靠性。
     **/
    public synchronized void subscribeTypeZero(String topic) {
        subscribe(topic, DATA_QOS_ZERO);
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 0018 19:39
     * @Param [topic]
     * @Description 订阅某个主题，qos为1：至少1次
     * 说明：当使用级别1时，它可以保证消息至少被送达到接收者一次，但也可能被送达到多次。
     **/
    public synchronized void subscribeTypeOne(String topic) {
        subscribe(topic, DATA_QOS_ZERO);
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 0018 19:39
     * @Param [topic]
     * @Description 订阅某个主题，qos为2：只有一次
     * 说明：QoS 2 是最高级别的，它可以保证每条消息只被接收一次。它是最安全的但也是最慢的服务级别。
     * 其通过发送者和接收者的两次对话来实现。
     **/
    public synchronized void subscribeTypeTwo(String topic) {
        subscribe(topic, DATA_QOS_TWO);
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 16:33
     * @Param [topic, qos]
     * @Description 订阅某个主题。
     **/
    private void subscribe(String topic, int qos) {
        /**1.不存在client，则新建一个client*/
        if (mqttClient == null) {
            createMqttClient();
        }
        /**2.未连接上，则建立一个连接*/
        if (!mqttClient.isConnected()) {
            connect();
        }
        /**3.订阅主题*/
        subscribe(mqttClient, topic, qos);
    }

    /**
     * @return void
     * @Author wangzhaosen@runhangtech.com
     * @Date 2019/1/18 16:27
     * @Param [mqttClient, topic, qos]
     * @Description 订阅主题。
     **/
    private void subscribe(MqttClient mqttClient, String topic, int qos) {
        try {
            if (mqttClient != null) {
                mqttClient.subscribe(topic, qos);
                //publishTypeTwo(topic,"hello");
            }
        } catch (MqttException e) {
            log.error("订阅主题报错，主题：'" + topic + "'" + e.getMessage());
        }
    }
}