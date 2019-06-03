package com.runhang.shadow.server.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName ParseXMLUtils
 * @Description 解析XML工具类
 * @Date 2019/5/18 10:28
 * @author szh
 **/
@Slf4j
public class ParseXMLUtils {

    /***
     * 校验xml文件格式
     *
     * @param xmlFile xml文件
     * @param xsdFile xsd文件
     * @return boolean 是否通过校验
     * @author szh
     * @date 2019/2/8 23:28
     */
    public static boolean domValidate(File xmlFile, File xsdFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(xmlFile);

            SchemaFactory constraintFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Source constraints = new StreamSource(xsdFile);
            Schema schema = constraintFactory.newSchema(constraints);

            Validator validator = schema.newValidator();

            try {
                validator.validate(new DOMSource(doc));
            } catch (org.xml.sax.SAXException e) {
                log.error("XML file validation error: " + e.getMessage());
                return false;
            }

        } catch (ParserConfigurationException e) {
            log.error("The underlying parser does not support the requested features.");
            return false;
        } catch (FactoryConfigurationError e) {
            log.error("Error occurred obtaining Document Builder Factory.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 由xml文件动态生成class
     *
     * @param xmlFile 用户上传的xml文件
     * @return java代码
     */
    public static  Map<String, String> xml2Class(File xmlFile) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(xmlFile);
            if (null == document) {
                return null;
            }
            Element root = document.getRootElement();   // 根元素
            Map<String, String> classCode = new HashMap<>();    // 所有类源码
            // 遍历节点下元素
            for (Iterator<Element> itClass = root.elementIterator(); itClass.hasNext(); ) {
                Element clazz = itClass.next();
                String className = clazz.attribute("type").getValue();
                Map<String, String> propertyMap = new HashMap<>();   // 类属性
                // 遍历属性
                for (Iterator<Element> itField = clazz.elementIterator(); itField.hasNext(); ) {
                    Element field = itField.next();
                    String attrName = field.getText();
                    switch (field.getName()) {
                        // 普通属性
                        case "field":
                            Attribute fieldAttrType = field.attribute("type");
                            if ( null != fieldAttrType) {
                                propertyMap.put(attrName, fieldAttrType.getValue());
                            }
                            break;

                        // 列表属性
                        case "list":
                            Attribute listAttrType = field.attribute("type");
                            if (null != listAttrType) {
                                propertyMap.put(attrName, "List<" + listAttrType.getValue() + ">");
                            }
                            break;

                        // map属性
                        case "map":
                            Attribute mapAttrKey = field.attribute("key");
                            Attribute mapAttrValue = field.attribute("value");
                            if (null != mapAttrKey && null != mapAttrValue) {
                                propertyMap.put(attrName, "Map<" + mapAttrKey.getValue() + ", " + mapAttrValue.getValue() + ">");
                            }
                            break;

                        default:
                            break;
                    }
                    String sourceStr = ClassUtils.generateCode(className, propertyMap);
                    classCode.put(className, sourceStr);
                }
            }

            // 编译
            return classCode;
        } catch (Exception e) {
            log.error("xml to class error: " + e.getMessage());
            return null;
        }
    }

}
