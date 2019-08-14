package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.common.utils.ClassUtils;
import com.runhang.shadow.client.core.bean.shadow.ShadowBean;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.exception.NoSriException;
import com.runhang.shadow.client.core.exception.NoTopicException;
import com.runhang.shadow.client.core.sync.database.DatabaseQueue;
import com.runhang.shadow.client.core.sync.push.ControlPush;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ShadowUtils
 * @Description 影子操作工具类
 * @Date 2019/7/4 9:46
 * @author szh
 **/
public class ShadowUtils {

    private static ControlPush controlPush = new ControlPush();

    /**
     * @Description 增加影子对象
     * @param data 影子对象
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/7/5 9:22
     */
    public static boolean addShadow(ShadowEntity data, String topic) throws NoTopicException, NoSriException {
        // 注入影子
        boolean shadowSuccess = ShadowFactory.injectShadow(data, topic);
        // 注入实体
        if (shadowSuccess) {
            List<String> entityNames = ClassUtils.getAllEntityName();
            EntityFactory.injectEntities(data, topic, entityNames);
        }
        return shadowSuccess;
    }

    /**
     * @Description 获取影子时加锁
     * @param shadowBean 影子
     * @return 影子对象
     * @author szh
     * @Date 2019/8/9 23:42
     */
    private static ShadowEntity dealGetShadow(ShadowBean shadowBean) {
        if (null != shadowBean) {
            String topic = shadowBean.getTopic();

            /** 获取影子对象的信号量 */
            Semaphore semaphore = ShadowFactory.getSemaphore(topic);
            /** 设置线程使用的topic*/
            String threadName = Thread.currentThread().getName();
            List<String> threadTopics = ShadowFactory.getThreadTopic(threadName);
            if (threadTopics == null) {
                threadTopics = new ArrayList<>();
                threadTopics.add(topic);
                Map<String, List<String>> threadMap = ShadowFactory.getThreadMap();
                threadMap.put(threadName, threadTopics);
            }
            threadTopics.add(topic);
//            System.out.println(Thread.currentThread().getName() + " 信号量余量 " + semaphore.availablePermits());
            try {
                if (null != semaphore) {
                    while (!semaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                        ShadowUtils.class.wait();
                    }
                    return shadowBean.getData();
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                semaphore.release();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @Description 获取影子对象
     * @param topic 主题
     * @return 对象
     * @author szh
     * @Date 2019/5/2 20:46
     */
    public static synchronized ShadowEntity getShadow(String topic) {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        return dealGetShadow(shadowBean);

    }

    /**
     * @Description 按类型获取影子对象列表
     * @param dataClass 影子类型
     * @return 对象列表
     * @author szh
     * @Date 2019/8/8 19:13
     */
    public static List<ShadowEntity> getShadowList(Class<?> dataClass) {
        List<ShadowBean> shadowBeans = ShadowFactory.getShadowBeans(dataClass);
        List<ShadowEntity> shadowEntities = new ArrayList<>();

        for (ShadowBean bean : shadowBeans) {
            ShadowEntity entity = dealGetShadow(bean);
            if (null != entity) {
                shadowEntities.add(entity);
            }
        }
        return shadowEntities;
    }

    /**
     * @Description 持久化实体修改
     * @param topic 主题
     * @author szh
     * @Date 2019/7/22 16:30
     */
    public static void save(String topic) {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        if (null != shadowBean) {
            DatabaseQueue.amqpSave(shadowBean.getData());
        }
    }

    /**
     * @Description 通过主题更新影子文档
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/9 10:46
     */
    public static synchronized ReErrorCode commit(String topic) throws NoSriException {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        if (null != shadowBean) {
            long current = System.currentTimeMillis();
            return shadowBean.updateShadowByServer(current);
//        if (null == errorCode) {
//            // 保存到数据库
//            DatabaseQueue.amqpSave(shadowBean.getData());
//        }
        } else {
            return null;
        }
    }

    /**
     * @Description 通过主题更新影子文档并下发
     * @param topic 主题
     * @return 是否成功
     * @author szh
     * @Date 2019/5/2 16:17
     */
    public static synchronized ReErrorCode commitAndPush(String topic) throws NoSriException {
        ShadowBean shadowBean = ShadowFactory.getShadowBean(topic);
        if (null != shadowBean) {
            long current = System.currentTimeMillis();
            ReErrorCode error = shadowBean.updateShadowByServer(current);
            if (null == error) {
                // 保存到数据库
//            DatabaseQueue.amqpSave(shadowBean.getData());
                // 更新版本
                shadowBean.getDoc().addUpVersion();
                // 下发状态
                controlPush.push(topic, shadowBean.getDoc(), current);
            }
            return error;
        } else {
            return null;
        }
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
        if (null != shadowBean) {
            // 检查是否修改
            if (shadowBean.getDoc().getState().getDesired().isEmpty()) {
                return ReErrorCode.SHADOW_ATTR_NOT_MODIFIED;
            }
            // 更新版本
            shadowBean.getDoc().addUpVersion();
            // 下发
            long current = shadowBean.getDoc().getTimestamp();
            controlPush.push(topic, shadowBean.getDoc(), current);
        }
        return null;
    }

    /**
     * @Description 影子回滚
     * @param topic 主题
     * @author szh
     * @Date 2019/7/24 10:27
     */
    public static void revert(String topic) throws NoSriException {
        ShadowBean bean = ShadowFactory.getShadowBean(topic);
        if (null != bean) {
            bean.shadowRevert();
        }
    }

    /**
     * 释放信号量
     * @param topic 影子主题
     */
    public static synchronized void releaseSemaphore(String topic) {
        Semaphore semaphore = ShadowFactory.getSemaphore(topic);
        semaphore.release();
        ShadowUtils.class.notifyAll();
    }

    /**
     * @Description 通过sri获得实体
     * @param sri 影子标识
     * @return 实体
     * @author szh
     * @Date 2019/8/9 23:41
     */
    public static ShadowEntity getEntity(String sri) {
        return EntityFactory.getEntity(sri);
    }

    /**
     * @Description 增加实体
     * @param entity 实体
     * @author szh
     * @Date 2019/8/14 13:44
     */
    public static void addEntity(ShadowEntity entity) throws NoTopicException, NoSriException {
        List<String> entityNames = ClassUtils.getAllEntityName();
        if (StringUtils.isEmpty(entity.getEntityTopic())) {
            throw new NoTopicException();
        }
        EntityFactory.injectEntities(entity, entity.getEntityTopic(), entityNames);
    }

}
