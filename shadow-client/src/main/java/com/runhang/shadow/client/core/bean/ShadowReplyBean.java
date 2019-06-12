package com.runhang.shadow.client.core.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @ClassName ShadowReplyBean
 * @Description 影子通信回复bean
 * @Date 2019/4/28 20:58
 * @author szh
 **/
@Data
public class ShadowReplyBean {

    /** 回复类型 **/
    private String method;
    /** 回复信息载荷 **/
    private RePayload payload;
    /** 回复时间戳 **/
    private long timestamp;
    /** 影子版本 **/
    private int version;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
