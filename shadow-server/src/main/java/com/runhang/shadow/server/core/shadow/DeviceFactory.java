package com.runhang.shadow.server.core.shadow;

import com.runhang.shadow.server.common.utils.ClassUtils;
import com.runhang.shadow.server.common.utils.ParseXMLUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

/**
 * @ClassName DeviceFactory
 * @Description 设备bean工厂
 * @Date 2019/5/18 10:46
 * @author szh
 **/
@Slf4j
public class DeviceFactory {

    public void deviceInit() {
        File xmlFile = new File("src/main/resources/xmlData/model.xml");
        File xsdFile = new File("src/main/resources/xmlData/model.xsd");
        boolean validateSuccess = ParseXMLUtils.domValidate(xmlFile, xsdFile);
        if (validateSuccess) {
            Map<String, String> code = ParseXMLUtils.xml2ClassCode(xmlFile);
            if (null != code) {
                boolean success = ClassUtils.generateClass(code);
                log.info("generate classes: " + success);
            } else {
                log.error("generate code failed");
            }
        }

    }

}
