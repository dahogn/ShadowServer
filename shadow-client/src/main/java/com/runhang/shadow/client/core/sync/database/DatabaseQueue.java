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

    public static void amqpSave(ShadowEntity entity) {
        AmqpTemplate rabbitTemplate = BeanUtils.getBean(AmqpTemplate.class);
        rabbitTemplate.convertAndSend(DatabaseQueueConfig.TOPIC, entity);
    }

}
