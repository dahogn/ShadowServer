package com.runhang.shadow.client.core.sync.database;

import com.runhang.shadow.client.core.shadow.ShadowEntity;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName DatabaseQueueConfig
 * @Description 数据库队列配置
 * @Date 2019/7/3 19:52
 * @author szh
 **/
@Component
@RabbitListener(queues = DatabaseQueueConfig.TOPIC)
public class DatabaseQueueConfig {

    static final String TOPIC = "ShadowDatabaseQueue";

    @Bean
    public Queue databaseQueue() {
        return new Queue(TOPIC);
    }

    @RabbitHandler
    public void process(ShadowEntity entity) {
        DatabaseOperation.saveEntity(entity);
    }

}
