package com.runhang.shadow.client.core.sync.callback;

import com.runhang.shadow.client.core.bean.shadow.ShadowBean;
import com.runhang.shadow.client.core.bean.comm.ShadowOpsBean;
import com.runhang.shadow.client.core.sync.push.GetReplyPush;

/**
 * @ClassName ShadowGetCallback
 * @Description 获取影子的回调
 * @Date 2019/4/29 16:48
 * @author szh
 **/
public class ShadowGetCallback extends AbsMqttCallback {

    private GetReplyPush getReplyPush;

    public ShadowGetCallback() {
        this.getReplyPush = new GetReplyPush();
    }

    @Override
    public void dealMessage(ShadowOpsBean opsBean, ShadowBean shadowBean) {
        getReplyPush.push(shadowBean.getTopic(), shadowBean.getDoc(), shadowBean.getData().getClass());
    }

}
