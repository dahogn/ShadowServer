package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.core.model.DatabaseField;
import com.runhang.shadow.client.core.model.EntityField;

public interface ShadowObserver {

    /**
     * @Description 实体属性更新
     * @param data 数据库映射
     * @param field 实体属性
     * @author szh
     * @Date 2019/6/19 16:56
     */
    void onFieldUpdate(DatabaseField data, EntityField field);

}
