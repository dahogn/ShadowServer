package com.runhang.shadow.client.device.init;

import com.runhang.shadow.client.core.shadow.ShadowFactory;
import com.runhang.shadow.client.device.entity.Vending;
import com.runhang.shadow.client.device.repository.VendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ShadowInit
 * @Description 影子初始化
 * @Date 2019/6/12 15:28
 * @author szh
 **/
@Component
@Slf4j
public class ShadowInit implements CommandLineRunner {

    @Value("${shadow.auto-init}")
    private boolean autoInit;

    @Autowired
    private VendingRepository vendingRepository;

    @Override
    public void run(String... args) throws Exception {
        if (autoInit) {
            Map<String, Object> dataMap = new HashMap<>();
            List<Vending> vendingList = vendingRepository.findAll();
            for (Vending v : vendingList) {
                dataMap.put(v.getTopic(), v);
            }
            boolean injectResult = ShadowFactory.batchInjectShadow(dataMap);
            if (injectResult) {
                log.info("inject success!");
            }
        }
    }
}
