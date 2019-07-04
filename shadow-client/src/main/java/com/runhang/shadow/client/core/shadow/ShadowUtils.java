package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.core.bean.shadow.ShadowBean;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.sync.database.DatabaseQueue;
import com.runhang.shadow.client.core.sync.push.ControlPush;
import com.runhang.shadow.client.device.entity.ShadowEntity;

import java.util.concurrent.Semaphore;

/**
 * @ClassName ShadowUtils
 * @Description 影子操作工具类
 * @Date 2019/7/4 9:46
 * @author szh
 **/
public class ShadowUtils {

    private static ControlPush controlPush = new ControlPush();

    private static int SEMAPHORE = 1;

    //门闩 控制获取影子对象
    private static Semaphore semaphore = new Semaphore(SEMAPHORE);

    /**
     * @Description 获取影子对象
     * @param topic 主题
     * @return 对象
     * @author szh
     * @Date 2019/5/2 20:46
     */
    public static ShadowEntity getShadow(String topic) {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        if (null != shadowBean) {
            return shadowBean.getData();
        } else {
            return null;
        }
    }

    /**
     * @Description 通过主题更新影子文档
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/9 10:46
     */
    public static ReErrorCode commit(String topic) {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        long current = System.currentTimeMillis();
        ReErrorCode errorCode = shadowBean.updateShadowByServer(current);
        if (null == errorCode) {
            // 保存到数据库
            DatabaseQueue.amqpSave(shadowBean.getData());
        }
        semaphore.release();
        return errorCode;
    }

    /**
     * @Description 通过主题更新影子文档并下发
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/2 16:17
     */
    public static ReErrorCode commitAndPush(String topic) {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        long current = System.currentTimeMillis();
        ReErrorCode error = shadowBean.updateShadowByServer(current);
        if (null == error) {
            // 保存到数据库
            DatabaseQueue.amqpSave(shadowBean.getData());
            // 更新版本
            shadowBean.getDoc().addUpVersion();
            // 下发状态
            controlPush.push(topic, shadowBean.getDoc(), current);
        }

        return error;
    }

    /**
     * @Description 下发影子修改
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/9 10:44
     */
    public static ReErrorCode push(String topic) {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        // 检查是否修改
        if (shadowBean.getDoc().getState().getDesired().isEmpty()) {
            return ReErrorCode.SHADOW_ATTR_NOT_MODIFIED;
        }
        // 更新版本
        shadowBean.getDoc().addUpVersion();
        // 下发
        long current = shadowBean.getDoc().getTimestamp();
        controlPush.push(topic, shadowBean.getDoc(), current);
        return null;
    }

}
