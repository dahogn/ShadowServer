package com.runhang.shadow.server.common.utils;

import com.runhang.shadow.server.core.model.DatabaseField;
import com.runhang.shadow.server.core.model.ShadowCode;
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
import java.util.*;

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
     * 由xml文件动态生成class代码
     *
     * @param xmlFile 用户上传的xml文件
     * @return java代码
     */
    public static ShadowCode xml2ClassCode(File xmlFile) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(xmlFile);
            if (null == document) {
                return null;
            }
            Element root = document.getRootElement();   // 根元素

            Map<String, String> entityCode = new HashMap<>();   // 实体类源码
            Map<String, String> repositoryCode = new HashMap<>();   // 数据库映射源码
            Map<String, String> initCode = new HashMap<>();     // 初始化代码
            List<String> deviceName = new ArrayList<>();    // 使用影子平台管理的设备类

            // 遍历节点下元素生成类代码
            for (Iterator<Element> itClass = root.elementIterator(); itClass.hasNext(); ) {
                Element clazz = itClass.next();
                String className = clazz.attribute("name").getValue();
                // 判断是否为设备对象
                Attribute isDeviceAttr = clazz.attribute("device");
                boolean isDevice = null != isDeviceAttr && "true".equals(isDeviceAttr.getValue());

                Map<String, String> propertyMap = new HashMap<>();   // 类属性
                Map<String, DatabaseField> databaseFieldMap = new HashMap<>();  // 类属性与数据库字段映射关系

                if (isDevice) {
                    deviceName.add(className);
                    propertyMap.put("topic", "String");
                }

                // 遍历属性，记录属性名及类型，以及与数据库字段对应关系
                for (Iterator<Element> itField = clazz.elementIterator(); itField.hasNext(); ) {
                    Element field = itField.next();
                    String attrName = field.getText();
                    dealClassAttribute(attrName, field, databaseFieldMap, propertyMap);
                }
                // 生成代码
                String sourceStr = ClassUtils.generateEntityCode(className, propertyMap, databaseFieldMap);
                entityCode.put(className, sourceStr);
                String repositoryStr = ClassUtils.generateRepositoryCode(className);
                repositoryCode.put(DatabaseUtils.generateRepositoryName(className), repositoryStr);
            }

            // 生成初始化代码
            String init = ClassUtils.generateInitCode(deviceName);
            initCode.put("ShadowInit", init);

            // 返回源码
            return new ShadowCode(entityCode, repositoryCode, initCode);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("xml to class error: " + e.getMessage());
            return null;
        }
    }

    /**
     * @Description 解析类属性
     * @param attrName 属性名
     * @param field xml节点
     * @param databaseFieldMap 数据库字段对应关系
     * @param propertyMap 类属性
     * @author szh
     * @Date 2019/6/11 18:37
     */
    private static void dealClassAttribute(String attrName,
                                    Element field,
                                    Map<String, DatabaseField> databaseFieldMap,
                                    Map<String, String> propertyMap) {
        switch (field.getName()) {
            // id
            case "id":
                Attribute idAttrTable = field.attribute("table");
                Attribute idAttrColumn = field.attribute("column");
                String idTable = "";
                if (null != idAttrTable) {
                    idTable = idAttrTable.getValue();
                }
                String idColumn = "";
                if (null != idAttrColumn) {
                    idColumn = idAttrColumn.getValue();
                }
                databaseFieldMap.put("id", new DatabaseField(idTable, idColumn));
                break;

            // 普通属性
            case "field":
                Attribute fieldAttrType = field.attribute("type");
                if ( null != fieldAttrType) {
                    propertyMap.put(attrName, fieldAttrType.getValue());
                }
                // 数据库字段对应关系
                Attribute fieldAttrTable = field.attribute("table");
                Attribute fieldAttrColumn = field.attribute("column");
                String fieldTable = "";
                if (null != fieldAttrTable) {
                    fieldTable = fieldAttrTable.getValue();
                }
                String fieldColumn = "";
                if (null != fieldAttrColumn) {
                    fieldColumn = fieldAttrColumn.getValue();
                }
                databaseFieldMap.put(attrName, new DatabaseField(fieldTable, fieldColumn));
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
    }

}
