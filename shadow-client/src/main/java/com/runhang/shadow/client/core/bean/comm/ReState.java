package com.runhang.shadow.client.core.bean.comm;

import com.runhang.shadow.client.core.bean.shadow.ShadowDesiredDoc;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import lombok.Data;

/**
 * @ClassName ReState
 * @Description 影子通信设备状态
 * @Date 2019/7/1 11:10
 * @author szh
 **/
@Data
public class ReState {

    /** 设备上报数据 **/
    private ShadowEntity reported;
    /** 服务器期望数据 **/
    private ShadowDesiredDoc desired;

    public ReState() {
    }

    public ReState(ShadowDesiredDoc desired) {
        this.desired = desired;
    }

    public ReState(ShadowEntity reported, ShadowDesiredDoc desired) {
        this.reported = reported;
        this.desired = desired;
    }

}
