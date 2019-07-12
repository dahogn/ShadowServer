package com.runhang.shadow.client.core.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.runhang.shadow.client.core.enums.EntityOperation;
import lombok.Data;

import java.util.Map;

/**
 * @ClassName ShadowField
 * @Description 影子属性变化
 * @Date 2019/6/19 11:47
 * @author szh
 **/
@Data
public class ShadowField {

    /** 类名 **/
    private String className;
    /** 实体标识 **/
    private String sri;
    /** 父级sri **/
    private String parentSri;
    /** 改变的属性名（适用于增删list属性） **/
    private String fieldName;
    /** 变化属性（适用于修改属性） **/
    private Map<String, Object> field;
    /** 变化属性的原值（适用于修改属性） **/
    @JSONField(serialize = false)
    private Map<String, Object> originalField;
    /** 变化操作（不序列化） **/
    @JSONField(serialize = false)
    private EntityOperation operation;

    public ShadowField() {
    }

    public ShadowField(String className, String sri, String fieldName, String parentSri, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.fieldName = fieldName;
        this.parentSri = parentSri;
        this.operation = operation;
    }

    public ShadowField(String className, String sri, Map<String, Object> field, Map<String, Object> originalField, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.field = field;
        this.originalField = originalField;
        this.operation = operation;
    }

    public ShadowField(String className, String sri, String fieldName, String parentSri, Map<String, Object> field, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.fieldName = fieldName;
        this.parentSri = parentSri;
        this.field = field;
        this.operation = operation;
    }

}
