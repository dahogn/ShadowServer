package com.runhang.shadow.client.core.bean;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ShadowDocData
 * @Description 影子文档中状态
 * @Date 2019/4/28 0:01
 * @author szh
 **/
@Data
public class ShadowDocData {

    /** 设备上报数据 **/
    private Map<String, Object> reported = new HashMap<>();
    /** 服务器期望数据 **/
    private Map<String, Object> desired = new HashMap<>();

    public ShadowDocData() {
    }

    public ShadowDocData(Map<String, Object> reported, Map<String, Object> desired) {
        this.reported = reported;
        this.desired = desired;
    }

}
