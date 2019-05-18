package com.runhang.shadow.demo.core.shadow;

import com.runhang.shadow.demo.common.utils.ClassUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeviceFactoryTest {

    @Test
    public void deviceInit() {

        DeviceFactory deviceFactory = new DeviceFactory();
        deviceFactory.deviceInit();

        try {
            Class penClass = Class.forName(ClassUtils.PACKAGE_NAME + ".Pen");
            Object pen = penClass.newInstance();
            ClassUtils.setValue(pen, "price", 1.1);
            System.out.println("pen's price: " + ClassUtils.getValue(pen, "price"));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}