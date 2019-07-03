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
    /** 改变的属性名 **/
    private String fieldName;
    /** 父级sri **/
    private String parent;
    /** 变化属性 **/
    private Map<String, Object> field;
    /** 变化操作（不序列化） **/
    @JSONField(serialize = false)
    private EntityOperation operation;

    public ShadowField() {
    }

    public ShadowField(String className, String sri, String fieldName, String parent, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.fieldName = fieldName;
        this.parent = parent;
        this.operation = operation;
    }

    public ShadowField(String className, String sri, Map<String, Object> field, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.field = field;
        this.operation = operation;
    }

    public ShadowField(String className, String sri, String fieldName, String parent, Map<String, Object> field, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.fieldName = fieldName;
        this.parent = parent;
        this.field = field;
        this.operation = operation;
    }

}
