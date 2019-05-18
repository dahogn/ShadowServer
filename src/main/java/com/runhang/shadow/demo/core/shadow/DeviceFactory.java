package com.runhang.shadow.demo.core.shadow;

import com.runhang.shadow.demo.common.utils.ParseXMLUtils;

import java.io.File;

/**
 * @ClassName DeviceFactory
 * @Description 设备bean工厂
 * @Date 2019/5/18 10:46
 * @author szh
 **/
public class DeviceFactory {

    public void deviceInit() {
        File xmlFile = new File("src/main/resources/xmlData/model.xml");
        File xsdFile = new File("src/main/resources/xmlData/model.xsd");
        boolean validateSuccess = ParseXMLUtils.domValidate(xmlFile, xsdFile);
        System.out.println("validate: " + validateSuccess);
        if (validateSuccess) {
            boolean success = ParseXMLUtils.xml2Class(xmlFile);
            System.out.println("generate: " + success);
        }

    }

}
