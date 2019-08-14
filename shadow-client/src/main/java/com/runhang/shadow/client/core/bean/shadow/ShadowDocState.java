package com.runhang.shadow.client.core.bean.shadow;

import com.alibaba.fastjson.JSONObject;
import com.runhang.shadow.client.core.shadow.ShadowEntity;

/**
 * @ClassName ShadowDocState
 * @Description 影子文档上报数据
 * @Date 2019/5/5 9:27
 * @author szh
 **/
public class ShadowDocState {

    /** 设备上报数据 **/
    private String reported = "{}";
    /** 服务器期望数据 **/
    private ShadowDesiredDoc desired;

    public ShadowDocState() {
        desired = new ShadowDesiredDoc();
    }

    public Object getReported(Class<?> dataClass) {
        return JSONObject.parseObject(reported, dataClass);
    }

    public String getReportedStr() {
        return reported;
    }

    public void setReported(ShadowEntity reported) {
        this.reported = JSONObject.toJSONString(reported);
    }

    public void setReportedStr(String reported) {
        this.reported = reported;
    }

    public ShadowDesiredDoc getDesired() {
        return desired;
    }

    public void setDesired(ShadowDesiredDoc desired) {
        this.desired = desired;
    }

}
