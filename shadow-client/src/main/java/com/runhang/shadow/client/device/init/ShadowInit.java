package com.runhang.shadow.client.device.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @ClassName ShadowInit
 * @Description 影子初始化
 * @Date 2019/6/12 15:28
 * @author szh
 **/
@Component
public class ShadowInit implements CommandLineRunner {

    @Value("${shadow.auto-init}")
    private boolean autoInit;

    private String[] deviceList = {"Vending"};

    @Override
    public void run(String... args) throws Exception {
        if (autoInit) {

        }
    }
}
