package com.runhang.shadow.client.core.sync.database;

import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import org.springframework.amqp.core.AmqpTemplate;

/**
 * @ClassName DatabaseQueue
 * @Description 数据库操作队列
 * @Date 2019/7/3 19:43
 * @author szh
 **/
public class DatabaseQueue {

    /**
     * @Description 异步数据库存储
     * @param entity 操作实体
     * @author szh
     * @Date 2019/7/11 11:16
     */
    public static void amqpSave(ShadowEntity entity) {
        AmqpTemplate rabbitTemplate = BeanUtils.getBean(AmqpTemplate.class);
        rabbitTemplate.convertAndSend(DatabaseQueueConfig.TOPIC, entity);
    }

}
