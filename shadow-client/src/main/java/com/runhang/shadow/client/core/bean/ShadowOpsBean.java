package com.runhang.shadow.client.core.bean;

import lombok.Data;

/**
 * @ClassName ShadowOpsBean
 * @Description 影子通信操作bean
 * @Date 2019/4/28 20:50
 * @author szh
 **/
@Data
public class ShadowOpsBean {

    /** 操作类型 **/
    private String method;
    /** 设备状态 **/
    private ShadowDocData state;
    /** 设备版本 **/
    private int version;

}
