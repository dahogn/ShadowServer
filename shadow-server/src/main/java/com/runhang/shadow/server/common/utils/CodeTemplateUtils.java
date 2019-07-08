package com.runhang.shadow.server.common.utils;

import com.runhang.shadow.server.core.shadow.EntityFactory;
import com.runhang.shadow.server.core.shadow.ShadowFactory;
import com.runhang.shadow.server.core.model.DatabaseField;
import com.runhang.shadow.server.device.entity.ShadowEntity;
import com.squareup.javapoet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.runhang.shadow.server.common.utils.ClassUtils.*;

/**
 * @ClassName CodeTemplateUtils
 * @Description 代码模板生成工具
 * @Date 2019/7/4 15:36
 * @author szh
 **/
@Slf4j
public class CodeTemplateUtils {

    /**
     * @Description 由属性及数据类型键值对生成java代码
     * @param className 类名
     * @param propertyMap 属性定义
     * @param databaseFieldMap 数据库字段映射
     * @param classSet 所有实体类名
     * @return java代码
     * @author szh
     * @Date 2019/7/7 22:21
     */
    public static String generateEntityCode(String className, Map<String, String> propertyMap,
                                      Map<String, DatabaseField> databaseFieldMap,
                                      Set<String> classSet) {
        try {
            // 生成类
            TypeSpec.Builder entityBuilder = TypeSpec.classBuilder(className)
                    .addAnnotation(Entity.class)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ShadowEntity.class);

            // 数据库字段初始化静态代码
            CodeBlock.Builder databaseStaticBuilder = CodeBlock.builder();
            for (String field : databaseFieldMap.keySet()) {
                DatabaseField databaseField = databaseFieldMap.get(field);
                databaseStaticBuilder.addStatement("databaseFieldMap.put($S, new DatabaseField($S, $S))",
                        field, databaseField.getTable(), databaseField.getColumn());
            }
            CodeBlock databaseStatic = databaseStaticBuilder.build();
            entityBuilder.addStaticBlock(databaseStatic);

            // 数据库字段映射属性
            FieldSpec databaseMap = FieldSpec
                    .builder(ParameterizedTypeName.get(Map.class, String.class, com.runhang.shadow.client.core.model.DatabaseField.class), "databaseFieldMap")
                    .addAnnotation(Transient.class)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new $T<>()", HashMap.class)
                    .build();
            entityBuilder.addField(databaseMap);

            // 属性 & getter & setter
            for (String fieldName : propertyMap.keySet()) {
                // 1. 属性字段
                String type = propertyMap.get(fieldName);
                FieldSpec field;
                ParameterSpec setterParam;

                if (type.startsWith("List")) {
                    // list属性增加表的关联映射
                    String entityType = type.substring(type.indexOf("<") + 1, type.indexOf(">"));

                    AnnotationSpec oneToManyAnno = AnnotationSpec
                            .builder(OneToMany.class)
                            .addMember("fetch", "$T.EAGER", FetchType.class)
                            .addMember("cascade", "$T.ALL", CascadeType.class)
                            .build();
                    AnnotationSpec joinColumnAnno = AnnotationSpec
                            .builder(JoinColumn.class)
                            .addMember("name", "$S", DatabaseUtils.generateForeignKey(className))
                            .build();
                    field = FieldSpec
                            .builder(ParameterizedTypeName.get(
                                    ClassName.get(List.class),
                                    ClassName.get(ENTITY_PACKAGE_NAME, entityType)),
                                    fieldName)
                            .addAnnotation(oneToManyAnno)
                            .addAnnotation(joinColumnAnno)
                            .addModifiers(Modifier.PRIVATE)
                            .build();

                    setterParam = ParameterSpec
                            .builder(ParameterizedTypeName.get(
                                    ClassName.get(List.class),
                                    ClassName.get(ENTITY_PACKAGE_NAME, entityType)),
                                    fieldName)
                            .build();
                } else if (classSet.contains(type)) {
                    // 嵌套单个实体增加映射
                    AnnotationSpec oneToOneAnno = AnnotationSpec
                            .builder(OneToOne.class)
                            .addMember("fetch", "$T.EAGER", FetchType.class)
                            .build();
                    field = FieldSpec
                            .builder(ClassName.get(ENTITY_PACKAGE_NAME, type), fieldName)
                            .addAnnotation(oneToOneAnno)
                            .addModifiers(Modifier.PRIVATE)
                            .build();

                    setterParam = ParameterSpec
                            .builder(ClassName.get(ENTITY_PACKAGE_NAME, type), fieldName)
                            .build();
                } else {
                    // 普通属性
                    String fieldType;
                    if ("int".equals(type)) {
                        fieldType = "Integer";
                    } else {
                        fieldType = Character.toUpperCase(type.charAt(0)) + type.substring(1);
                    }
                    field = FieldSpec
                            .builder(ClassName.get("java.lang", fieldType), fieldName)
                            .addModifiers(Modifier.PRIVATE)
                            .build();

                    setterParam = ParameterSpec.builder(ClassName.get("java.lang", fieldType), fieldName).build();
                }
                entityBuilder.addField(field);

                // 2. getter
                MethodSpec getterMethod = MethodSpec
                        .methodBuilder(ClassUtils.getGetterSetterName(fieldName, ClassUtils.METHOD_GETTER))
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(ENTITY_PACKAGE_NAME, type))
                        .addStatement("return $L", fieldName)
                        .build();
                entityBuilder.addMethod(getterMethod);

                // 3. setter
                MethodSpec setterMethod = MethodSpec
                        .methodBuilder(ClassUtils.getGetterSetterName(fieldName, ClassUtils.METHOD_SETTER))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(setterParam)
                        .addStatement("this.$L = $L", fieldName, fieldName)
                        .build();
                entityBuilder.addMethod(setterMethod);
            }

            TypeSpec entity = entityBuilder.build();
            JavaFile javaFile = JavaFile.builder(ENTITY_PACKAGE_NAME, entity).build();

            return javaFile.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Description 生成数据库映射代码
     * @param entityName 实体名
     * @return java代码
     * @author szh
     * @Date 2019/7/8 13:58
     */
    public static String generateRepoCode(String entityName) {
        try {
            String repoName = DatabaseUtils.generateRepositoryName(entityName);

            TypeSpec repoInter = TypeSpec
                    .interfaceBuilder(repoName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get(JpaRepository.class),
                            ClassName.get(ENTITY_PACKAGE_NAME, entityName),
                            ClassName.get(Integer.class)))
                    .build();

            JavaFile javaFile = JavaFile.builder(REPOSITORY_PACKAGE_NAME, repoInter).build();
            return javaFile.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * @Description 生成初始化代码
     * @param entityList 实体list
     * @param deviceList 设备list
     * @return java代码
     * @author szh
     * @Date 2019/7/8 13:59
     */
    public static String generateInitCode(Set<String> entityList, List<String> deviceList) {
        try {
            // 生成类
            TypeSpec.Builder initBuilder = TypeSpec
                    .classBuilder("ShadowInit")
                    .addAnnotation(Component.class)
                    .addAnnotation(Slf4j.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(CommandLineRunner.class);

            // 是否自动注入
            AnnotationSpec valueAnno = AnnotationSpec
                    .builder(Value.class)
                    .addMember("value", "$S", "${shadow.auto-init}")
                    .build();
            FieldSpec autoInitField = FieldSpec
                    .builder(Boolean.class, "autoInit")
                    .addModifiers(Modifier.PRIVATE)
                    .addAnnotation(valueAnno)
                    .build();
            initBuilder.addField(autoInitField);

            // repository属性
            for (String entityName : entityList) {
                String repoType = DatabaseUtils.generateRepositoryName(entityName);
                String repoName = ClassUtils.generateFieldName(repoType);
                FieldSpec repositoryField = FieldSpec
                        .builder(ClassName.get(REPOSITORY_PACKAGE_NAME, repoType), repoName)
                        .addAnnotation(Autowired.class)
                        .addModifiers(Modifier.PRIVATE)
                        .build();
                initBuilder.addField(repositoryField);
            }

            // 初始化方法
            MethodSpec.Builder initMethodBuilder = MethodSpec
                    .methodBuilder("run")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(String[].class, "args")
                    .addException(Exception.class)
                    .beginControlFlow("if (autoInit)")
                    .addStatement("$T dataMap = new $T<>()", ParameterizedTypeName.get(Map.class, String.class, ShadowEntity.class), HashMap.class);

            //
            for (String device : deviceList) {
                String deviceRepoName = ClassUtils.generateFieldName(DatabaseUtils.generateRepositoryName(device));
                String listName = ClassUtils.generateFieldName(device) + "List";
                initMethodBuilder.addStatement("$T " + listName + " = $L.findAll()",
                        ParameterizedTypeName.get(
                                ClassName.get(List.class),
                                ClassName.get(ENTITY_PACKAGE_NAME, device)),
                        deviceRepoName);
            }

            initMethodBuilder.addStatement("$T.destroyEntities()", EntityFactory.class);
            initMethodBuilder.addStatement("$T entityNames = $T.getAllEntityName()",
                    ParameterizedTypeName.get(List.class, String.class),
                    com.runhang.shadow.client.common.utils.ClassUtils.class);

            for (String device : deviceList) {
                String deviceName = ClassUtils.generateFieldName(device);
                String listName = ClassUtils.generateFieldName(device) + "List";
                initMethodBuilder.beginControlFlow("for ($T $L : $L)", ClassName.get(ENTITY_PACKAGE_NAME, device), deviceName, listName);
                initMethodBuilder.addStatement("dataMap.put($L.getTopic(), $L)", deviceName, deviceName);
                initMethodBuilder.addStatement("$T.injectEntities($L, $L.getTopic(), entityNames)", EntityFactory.class, deviceName, deviceName);
                initMethodBuilder.endControlFlow();
            }

            initMethodBuilder.addStatement("boolean injectResult = $T.batchInjectShadow(dataMap)", ShadowFactory.class);

            initMethodBuilder.endControlFlow();
            initBuilder.addMethod(initMethodBuilder.build());

            JavaFile javaFile = JavaFile.builder(INIT_PACKAGE_NAME, initBuilder.build()).build();

            return javaFile.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
