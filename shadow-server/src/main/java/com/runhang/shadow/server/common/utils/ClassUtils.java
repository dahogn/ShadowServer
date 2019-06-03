package com.runhang.shadow.server.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.tools.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

/**
 * @ClassName ClassUtils
 * @Description 类操作工具类
 * @Date 2019/5/18 10:22
 * @author szh
 **/
@Slf4j
public class ClassUtils {

    // getter & setter 选项
    private static final String METHOD_GETTER = "get";
    private static final String METHOD_SETTER = "set";

    private static final String CLASS_FILE_PATH = "target/classes";    // 编译生成的class文件路径
    private static final String JAVA_FILE_PATH = "src/main/java/com/runhang/shadow/demo/device/entity/";    // java文件路径
    public static final String ENTITY_PACKAGE_NAME = "com.runhang.shadow.demo.device.entity";  // 动态类的包名
    public static final String INTERFACE_PACKAGE_NAME = "com.runhang.shadow.demo.device.mapper";

    /**
     * 由属性及数据类型键值对生成java代码
     *
     * @param className 类名
     * @param propertyMap 属性定义
     * @return java代码
     */
    public static String generateCode(String className, Map<String, String> propertyMap) {
        String codeStr = "package " + ENTITY_PACKAGE_NAME + ";\n" +  // 包
                "import java.util.List;\nimport java.util.Map;\n" +    // 导包
                "public class " + className + "{\n";
        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            String field = entry.getKey();
            String type = entry.getValue();
            codeStr += "private " + type + " " + field + ";\n";   // 属性
            codeStr += "public void " + getGetterSetterName(field, METHOD_SETTER) + "(Object " + field + ") {\n";// setter
            codeStr += "this." + field + " = (" + type + ") " + field + "; \n}\n";
            codeStr += "public " + type + " " + getGetterSetterName(field, METHOD_GETTER) + "() {\n";   // getter
            codeStr += "return " + field + "; \n}\n";
        }
        codeStr += "}";
        return codeStr;
    }

    public static String generateInterface(String className, Map<String, String> propertyMap) {
        String codeStr = "package " + ENTITY_PACKAGE_NAME + ";\n" +  // 包
                "import java.util.List;\nimport java.util.Map;\n" +    // 导包
                "public interface " + className + "{\n";
        for (String field : propertyMap.keySet()) {
            String type = propertyMap.get(field);
        }
        return "";
    }

    /**
     * 单个生成java文件并编译
     *
     * @param sourceStr java代码
     * @param className 类名
     * @return 是否编译成功
     */
    public static boolean generateClass(String sourceStr, String className) {

        try {
            // 创建java文件并写入代码
//            File javaFile = new File(JAVA_FILE_PATH + className + ".java");
//            if (!javaFile.exists()) {
//                javaFile.getParentFile().mkdirs();
//                javaFile.createNewFile();
//            }
//            OutputStream os = new FileOutputStream(javaFile);
//            os.write(sourceStr.getBytes(), 0, sourceStr.length());
//            os.flush();
//            os.close();

            // 当前编译器
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            // Java标准文件管理器
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            // Java文件对象
            JavaFileObject fileObject = new StringJavaObject(className, sourceStr);
            // 编译参数，类似于Javac <options> 中的options，编译文件的存放地方
            List<String> optionList = new ArrayList<>(Arrays.asList("-d", CLASS_FILE_PATH));
            // 要编译的单元
            List<JavaFileObject> fileObjectList = Arrays.asList(fileObject);
            // 设置编译环境
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, optionList, null, fileObjectList);
            return task.call();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 批量生成java文件并编译
     * @param classCode 类名及代码
     * @return
     */
    public static boolean generateClass(Map<String, String> classCode) {
        List<JavaFileObject> fileObjectList = new ArrayList<>();    // 要编译的单元
        for (Map.Entry<String, String> entry : classCode.entrySet()) {
            try {
                // 创建java文件并写入代码
                File javaFile = new File(JAVA_FILE_PATH + entry.getKey() + ".java");
                if (!javaFile.exists()) {
                    javaFile.getParentFile().mkdirs();
                    javaFile.createNewFile();
                }
                OutputStream os = new FileOutputStream(javaFile);
                os.write(entry.getValue().getBytes(), 0, entry.getValue().length());
                os.flush();
                os.close();
                // Java文件对象
                fileObjectList.add(new StringJavaObject(entry.getKey(), entry.getValue()));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        // 当前编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // Java标准文件管理器
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        // 编译参数，类似于Javac <options> 中的options，编译文件的存放地方
        List<String> optionList = new ArrayList<>(Arrays.asList("-d", CLASS_FILE_PATH));
        // 设置编译环境
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, optionList, null, fileObjectList);
        boolean compileResult = task.call();
        if (!compileResult) {
            // 编译失败删除java文件
            deleteJavaFiles(classCode.keySet());
        }
        return compileResult;
    }

    /**
     * @Description 删除生成的java文件
     * @param fileName 文件名
     * @author szh
     * @Date 2019/5/20 9:38
     */
    private static void deleteJavaFiles(Set<String> fileName) {
        for (String name : fileName) {
            File javaFile = new File(JAVA_FILE_PATH + name + ".java");
            FileUtils.deleteQuietly(javaFile);
        }
    }

    /**
     * 获取getter或setter方法名称
     *
     * @param propertyName 属性名
     * @param method 方法类型：ClassGenerateUtils.METHOD_GETTER 或 ClassGenerateUtils.METHOD_SETTER
     * @return 方法名
     */
    public static String getGetterSetterName(String propertyName, String method) {
        String upperPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        return method + upperPropertyName;
    }

    /**
     * 获取生成的class的完整名称
     *
     * @param className 类名
     * @return 完整类名
     */
    public static String getClassFullName(String className) {
        return ENTITY_PACKAGE_NAME + "." + className;
    }

    /**
     * 设置bean的属性值
     *
     * @param obj       bean对象
     * @param fieldName 属性名
     * @param value     属性值
     * @return          是否成功
     */
    public static boolean setValue(Object obj, String fieldName, Object value) {
        Class clazz = obj.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取bean的属性值
     *
     * @param obj       bean对象
     * @param fieldName 属性名称
     * @return          属性值
     */
    public static Object getValue(Object obj, String fieldName) {
        Class clazz = obj.getClass();
        String methodName = getGetterSetterName(fieldName, METHOD_GETTER);
        Object returnValue;
        try {
            Method method = clazz.getMethod(methodName);
            returnValue = method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return returnValue;
    }

}

class StringJavaObject extends SimpleJavaFileObject {

    // 源代码
    private String content;

    // 遵循Java规范的类名及文件
    StringJavaObject(String _javaFileName, String _content) {
        super(_createStringJavaObjectUri(_javaFileName), Kind.SOURCE);
        content = _content;
    }

    // 产生一个URL资源库
    private static URI _createStringJavaObjectUri(String name) {
        // 注意此处没有设置包名
        return URI.create("String:///" + name + Kind.SOURCE.extension);
    }

    // 文本文件代码
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }

}
