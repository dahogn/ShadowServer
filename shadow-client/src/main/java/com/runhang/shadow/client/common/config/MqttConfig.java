package com.runhang.shadow.client.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName MqttConfig
 * @Description mqtt配置信息
 * @Date 2019/2/2 20:05
 * @Author szh
 **/

@Component
public class MqttConfig {

    @Value("${com.mqtt.host}")
    private String host;
    @Value("${com.mqtt.port}")
    private String port;
    @Value("${com.mqtt.username}")
    private String username;
    @Value("${com.mqtt.password}")
    private String password;
    @Value("${com.mqtt.timeout}")
    private int timeout;
    @Value("${com.mqtt.keepAlive}")
    private int keepAlive;
    @Value("${com.mqtt.preFix}")
    private String preFix;

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPreFix(){
        return preFix;
    }

    public int getTimeOut(){
        return timeout;
    }

    public int getKeepAlive(){
        return keepAlive;
    }

}
