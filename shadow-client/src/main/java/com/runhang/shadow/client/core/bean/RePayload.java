package com.runhang.shadow.client.core.bean;

import lombok.Data;

/**
 * @ClassName RePayload
 * @Description 影子通信回复载荷
 * @Date 2019/4/28 21:00
 * @author szh
 **/
@Data
public class RePayload {

    /** 回复状态 **/
    private String status;
    /** 影子版本 **/
    private int version;
    /** 回复内容 **/
    private ReContent content;
    /** 影子状态 **/
    private ShadowDocData state;
    /** 影子元数据 **/
    private ShadowDocData metadata;

    public RePayload() {
    }

    public RePayload(String status, int version) {
        this.status = status;
        this.version = version;
    }

    public RePayload(String status, ReContent content) {
        this.status = status;
        this.content = content;
    }

    public RePayload(String status, ShadowDocData state, ShadowDocData metadata) {
        this.status = status;
        this.state = state;
        this.metadata = metadata;
    }

}
