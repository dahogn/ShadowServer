package com.runhang.shadow.client.core.bean.shadow;

import com.alibaba.fastjson.annotation.JSONField;
import com.runhang.shadow.client.core.model.ShadowField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ShadowDesiredDoc
 * @Description 影子期望文档
 * @Date 2019/6/25 11:13
 * @author szh
 **/
@Data
public class ShadowDesiredDoc {

    /** 文档增加 */
    private List<ShadowField> add = new ArrayList<>();
    /** 删除文档，内容是对应实体的sri */
    private List<String> delete = new ArrayList<>();
    /** 文档更新 */
    private List<ShadowField> update = new ArrayList<>();

    /**
     * @Description 服务器期望是否为空
     * @return 是否为空
     * @author szh
     * @Date 2019/6/25 14:56
     */
    @JSONField(serialize = false)
    public boolean isEmpty() {
        return add.isEmpty() && delete.isEmpty() && update.isEmpty();
    }

    /**
     * @Description 清空期望
     * @author szh
     * @Date 2019/6/25 14:57
     */
    public void clearDesired() {
        add.clear();
        delete.clear();
        update.clear();
    }

}
