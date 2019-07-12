package com.runhang.shadow.client.core.model;

import lombok.Data;

/**
 * @ClassName EntityField
 * @Description 实体变化属性
 * @Date 2019/6/19 10:50
 * @author szh
 **/
@Data
public class EntityField {

    private String className;   // 实体类名
    private String fieldName;   // 实体属性名
    private Object fieldValue;  // 属性值
    private Object originalValue;  // 属性原值

    public EntityField() {
    }

    public EntityField(String className, String fieldName, Object originalValue) {
        this.className = className;
        this.fieldName = fieldName;
        this.originalValue = originalValue;
    }

}
