package com.runhang.shadow.client.core.bean;

import lombok.Data;

/**
 * @ClassName ShadowDoc
 * @Description 设备影子文档
 * @Date 2019/4/27 23:59
 * @author szh
 **/
@Data
public class ShadowDoc {

    /** 影子状态 **/
    private ShadowDocState state;
    /** 影子元数据 **/
    private ShadowDocData metadata;
    /** 最后修改时间戳 **/
    private long timestamp;
    /** 影子版本 **/
    private Integer version = 0;

    /**
     * @Description 版本递增
     * @author szh
     * @Date 2019/5/3 11:13
     */
    public void addUpVersion() {
        version += 1;
    }

    public ShadowDocData getStateTrans() {
        return new ShadowDocData(state.getReported(), state.getDesired());
    }

}
