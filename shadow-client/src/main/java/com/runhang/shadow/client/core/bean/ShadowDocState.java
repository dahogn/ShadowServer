package com.runhang.shadow.client.core.bean;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Object> desired = new HashMap<>();

    public Map<String, Object> getReported() {
        return JSONObject.parseObject(reported);
    }

    public String getReportedStr() {
        return reported;
    }

    public void setReported(Map<String, Object> reported) {
        this.reported = JSONObject.toJSONString(reported);
    }

    public void setReportedStr(String reported) {
        this.reported = reported;
    }

    public Map<String, Object> getDesired() {
        return desired;
    }

    public void setDesired(Map<String, Object> desired) {
        this.desired = desired;
    }

}
