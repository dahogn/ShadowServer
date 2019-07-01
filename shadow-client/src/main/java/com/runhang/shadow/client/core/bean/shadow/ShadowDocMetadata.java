package com.runhang.shadow.client.core.bean.shadow;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ShadowDocMetadata
 * @Description 影子文档中状态
 * @Date 2019/4/28 0:01
 * @author szh
 **/
@Data
public class ShadowDocMetadata {

    /** 设备上报数据 **/
    private Map<String, Object> reported = new HashMap<>();
    /** 服务器期望数据 **/
    private Map<String, Object> desired = new HashMap<>();

    public ShadowDocMetadata() {
    }

    public ShadowDocMetadata(Map<String, Object> reported, Map<String, Object> desired) {
        this.reported = reported;
        this.desired = desired;
    }

}
