package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.core.bean.shadow.ShadowBean;
import com.runhang.shadow.client.core.enums.EntityOperation;
import com.runhang.shadow.client.core.model.DatabaseField;
import com.runhang.shadow.client.core.model.EntityField;
import com.runhang.shadow.client.core.model.ShadowField;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName EntityDataObserver
 * @Description 影子实体观察者
 * @Date 2019/6/18 16:32
 * @author szh
 **/
public class EntityDataObserver implements ShadowObserver {

    /** 观察对象所属设备的topic **/
    private String shadowTopic;
    /** 观察对象的sri **/
    private String sri;

    EntityDataObserver() {

    }

    EntityDataObserver(String shadowTopic, String sri) {
        this.shadowTopic = shadowTopic;
        this.sri = sri;
    }

    @Override
    public void onFieldUpdate(DatabaseField data, EntityField field) {
        // 影子中记录变更
        ShadowBean bean = ShadowFactory.getShadowBean(shadowTopic);
        // 变化属性
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(field.getFieldName(), field.getFieldValue());
        // 属性原值
        Map<String, Object> originalFieldMap = new HashMap<>();
        originalFieldMap.put(field.getFieldName(), field.getOriginalValue());

        ShadowField shadowField = new ShadowField(field.getClassName(), sri, fieldMap, originalFieldMap, EntityOperation.UPDATE);
        bean.addModifiedField(shadowField);
    }

}
