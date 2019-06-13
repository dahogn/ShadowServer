package com.runhang.shadow.server.core.shadow;

import com.runhang.shadow.server.common.utils.ClassUtils;
import com.runhang.shadow.server.common.utils.ParseXMLUtils;
import com.runhang.shadow.server.core.model.ShadowCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

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
        boolean success = false;
        if (validateSuccess) {
            ShadowCode code = ParseXMLUtils.xml2ClassCode(xmlFile);
            if (null != code) {
                success = ClassUtils.compileCode(code.getEntityCode(), ClassUtils.ENTITY_FILE_PATH);
                success = ClassUtils.compileCode(code.getRepositoryCode(), ClassUtils.REPOSITORY_FILE_PATH);
                success = ClassUtils.compileCode(code.getInitCode(), ClassUtils.INIT_FILE_PATH);
                log.info("generate classes: " + success);
            } else {
                log.error("generate code failed");
            }
        }

    }

}
