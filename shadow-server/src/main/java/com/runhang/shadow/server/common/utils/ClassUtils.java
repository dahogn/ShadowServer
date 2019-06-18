package com.runhang.shadow.server.common.utils;

import com.runhang.shadow.server.core.model.DatabaseField;
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

    // 编译生成的class文件路径
    private static final String CLASS_FILE_PATH = "target/classes";

    private static final String DEVICE_JAVA_FILE_PATH = "src/main/java/com/runhang/shadow/server/device/";
    private static final String MAIN_PACKAGE_NAME = "com.runhang.shadow.server.";

    // java实体类文件路径
    public static final String ENTITY_FILE_PATH = DEVICE_JAVA_FILE_PATH + "entity/";
    // 数据库管理文件路径
    public static final String REPOSITORY_FILE_PATH = DEVICE_JAVA_FILE_PATH + "repository/";
    // 初始化类文件路径
    public static final String INIT_FILE_PATH = DEVICE_JAVA_FILE_PATH + "init/";

    // 动态类的包名
    private static final String ENTITY_PACKAGE_NAME = MAIN_PACKAGE_NAME + "device.entity";
    // 数据库映射包名
    private static final String REPOSITORY_PACKAGE_NAME = MAIN_PACKAGE_NAME + "device.repository";
    // 初始化类包名
    private static final String INIT_PACKAGE_NAME = MAIN_PACKAGE_NAME + "device.init";

    /**
     * 由属性及数据类型键值对生成java代码
     *
     * @param className 类名
     * @param propertyMap 属性定义
     * @param databaseFieldMap 数据库字段映射
     * @param classSet 所有实体类名
     * @return java代码
     */
    public static String generateEntityCode(String className, Map<String, String> propertyMap,
                                            Map<String, DatabaseField> databaseFieldMap,
                                            Set<String> classSet) {
        StringBuilder codeStr =
                new StringBuilder(
                        "package " + ENTITY_PACKAGE_NAME + ";\n\n" +  // 包
                        "import java.util.*;\n" +
                        "import javax.persistence.*;\n" +
                        "import " + MAIN_PACKAGE_NAME + "core.model.DatabaseField;\n\n" +   // 导包
                        "@Entity\n" +
                        "public class " + className + " extends ShadowEntity {\n\n" +
                        "@Transient\n" +
                        "public static Map<String, DatabaseField> databaseFieldMap;\n");

        // 数据库字段初始化静态代码
        codeStr.append("static {\ndatabaseFieldMap = new HashMap<>();\n");
        for (String field : databaseFieldMap.keySet()) {
            DatabaseField databaseField = databaseFieldMap.get(field);
            codeStr.append(String.format(
                    "databaseFieldMap.put(\"%s\", new DatabaseField(\"%s\", \"%s\"));\n",
                    field, databaseField.getTable(), databaseField.getColumn()));
        }
        codeStr.append("}\n\n");

        // 属性 & getter & setter
        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            String field = entry.getKey();
            String type = entry.getValue();
            // list属性增加表的关联映射
            if (type.startsWith("List")) {
                codeStr.append("@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)\n");
                codeStr.append(String.format("@JoinColumn(name = \"%s\")\n", DatabaseUtils.generateForeignKey(className)));
            }
            // 嵌套单个实体增加映射
            if (classSet.contains(type)) {
                codeStr.append("@OneToOne(fetch = FetchType.EAGER)\n");
            }
            codeStr.append(String.format(
                    "private %s %s;\n" +
                    "public void %s(%s %s) { this.%s = %s; }\n" +
                    "public %s %s() { return %s; }\n\n",
                    type, field,
                    getGetterSetterName(field, METHOD_SETTER), type, field, field, field,
                    type, getGetterSetterName(field, METHOD_GETTER), field)
            );
        }

        codeStr.append("}");
        return codeStr.toString();
    }

    /**
     * @Description 生成数据库操作接口
     * @param className 对应的实体类名
     * @return 代码
     * @author szh
     * @Date 2019/6/12 9:59
     */
    public static String generateRepositoryCode(String className) {
        String codeStr = "package " + REPOSITORY_PACKAGE_NAME + ";\n\n" +  // 包
                "import " + MAIN_PACKAGE_NAME + "device.entity.*;\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n\n" +    // 导包
                "public interface " + DatabaseUtils.generateRepositoryName(className) +
                " extends JpaRepository<" + className + ", Integer> {\n}";
        return codeStr;
    }

    /**
     * @Description 生成设备信息初始化代码
     * @param classList 设备名列表
     * @return 代码
     * @author szh
     * @Date 2019/6/12 20:50
     */
    public static String generateInitCode(List<String> classList) {
        StringBuilder builder = new StringBuilder("package " + INIT_PACKAGE_NAME + ";\n\n" +
                "import " + MAIN_PACKAGE_NAME + "common.utils.ClassUtils;\n" +      // 包
                "import " + MAIN_PACKAGE_NAME + "core.shadow.ShadowFactory;\n" +
                "import " + ENTITY_PACKAGE_NAME + ".*;\n" +
                "import " + REPOSITORY_PACKAGE_NAME + ".*;\n" +
                "import lombok.extern.slf4j.Slf4j;\n" +
                "import java.util.*;\n" +
                "import org.springframework.beans.factory.annotation.Value;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.boot.CommandLineRunner;\n" +
                "import org.springframework.stereotype.Component;\n\n" +
                "@Slf4j\n@Component\npublic class ShadowInit implements CommandLineRunner {\n" +
                "@Value(\"${shadow.auto-init}\")\nprivate boolean autoInit;\n\n" // 是否自动加载
        );

        // 数据库映射注入
        for (String className : classList) {
            String fieldType = DatabaseUtils.generateRepositoryName(className);
            builder.append("@Autowired\nprivate ")
                    .append(fieldType).append(" ")
                    .append(generateFieldName(fieldType)).append(";\n");
        }

        // 初始化代码
        builder.append("\n@Override\npublic void run(String... args) throws Exception {\n")
                .append("if (autoInit) {\n")
                .append("Map<String, Object> dataMap = new HashMap<>();\n");
        // 数据库查询实体
        for (String className : classList) {
            builder.append("List<").append(className).append("> ")
                    .append(generateFieldName(className)).append(" = ")
                    .append(generateFieldName(DatabaseUtils.generateRepositoryName(className)))
                    .append(".findAll();\n");
        }
        // 删除空实体
        builder.append("ShadowFactory.destroyEntities();\n")
                .append("List<String> entityNames = ClassUtils.getAllEntityName();\n");
        // 影子与实体注入
        for (String className : classList) {
            String field = "" + className.charAt(0);
            builder.append(String.format(
                    "for (%s %s : %s) {\n" +
                    "dataMap.put(%s.getTopic(), %s);\n" +
                    "ShadowFactory.injectEntities(%s, entityNames);\n}\n",
                    className, field, generateFieldName(className), field, field, field));
        }
        builder.append("ShadowFactory.batchInjectShadow(dataMap);\n");
        builder.append("}\n}\n}");

        return builder.toString();
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
//            File javaFile = new File(ENTITY_FILE_PATH + className + ".java");
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
    public static boolean compileCode(Map<String, String> classCode, String filePath) {
        List<JavaFileObject> fileObjectList = new ArrayList<>();    // 要编译的单元
        for (Map.Entry<String, String> entry : classCode.entrySet()) {
            try {
                // 创建java文件并写入代码
                File javaFile = new File(filePath + entry.getKey() + ".java");
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
            deleteJavaFiles(classCode.keySet(), filePath);
        }
        return compileResult;
    }

    /**
     * @Description 删除生成的java文件
     * @param fileName 文件名
     * @param filePath 文件路径
     * @author szh
     * @Date 2019/5/20 9:38
     */
    private static void deleteJavaFiles(Set<String> fileName, String filePath) {
        for (String name : fileName) {
            File javaFile = new File(filePath + name + ".java");
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
     * @Description 生成属性名
     * @param typeName 类型名称
     * @return 属性名
     * @author szh
     * @Date 2019/6/18 15:17
     */
    private static String generateFieldName(String typeName) {
        if (Character.isUpperCase(typeName.charAt(0))) {
            return typeName;
        } else {
            return Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
        }
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
