package com.runhang.shadow.client.core.model;

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
    private Map<String, Object> field;
    private EntityOperation operation;

    public ShadowField() {
    }

    public ShadowField(String className, String sri, Map<String, Object> field, EntityOperation operation) {
        this.className = className;
        this.sri = sri;
        this.field = field;
        this.operation = operation;
    }

}
