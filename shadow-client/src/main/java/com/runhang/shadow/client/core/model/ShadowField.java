package com.runhang.shadow.client.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String className;
    private String sri; // 实体标识
    private String parent;  // 父级sri
    private Map<String, Object> field;
    @JsonIgnore
    private EntityOperation operation;

    public ShadowField() {
    }

    public ShadowField(String className, String sri, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.operation = operation;
    }

    public ShadowField(String className, String sri, Map<String, Object> field, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.field = field;
        this.operation = operation;
    }

    public ShadowField(String className, String sri, String parent, Map<String, Object> field, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.parent = parent;
        this.field = field;
        this.operation = operation;
    }

}
