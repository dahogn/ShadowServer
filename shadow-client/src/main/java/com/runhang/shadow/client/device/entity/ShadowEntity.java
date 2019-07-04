package com.runhang.shadow.client.device.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.runhang.shadow.client.core.model.DatabaseField;
import com.runhang.shadow.client.core.shadow.EntityFactory;
import com.runhang.shadow.client.core.shadow.ShadowSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName ShadowEntity
 * @Description 影子实体
 * @Date 2019/6/16 16:49
 * @author szh
 **/
@Slf4j
@MappedSuperclass
public class ShadowEntity extends ShadowSubject implements Serializable {
    @Transient
    ReadWriteLock lock = new ReentrantReadWriteLock();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Shadow Resource Identifier
     * 影子资源标识符
     */
    @JSONField(name = "sri")
    @Column(name = "sri")
    private String SRI;

    /**
     * 实体所属的影子对象的topic
     */
    @JSONField(serialize = false)
    @Transient
    private String entityTopic;


    ShadowEntity() {

    }

    /**
     * @Description 初始化生成SRI并注入容器
     * @author szh
     * @Date 2019/6/16 19:54
     */
    public ShadowEntity(String topic) throws Exception {
        super();
        this.SRI = generateSRI();
        boolean injectRe = EntityFactory.injectEntity(this);
        //log.info("inject " + SRI + ": " + injectRe);
        this.entityTopic =  topic;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setSRI(String SRI) {
        this.SRI = SRI;
    }

    public String getSRI() {
        return SRI;
    }

    public String getEntityTopic() {
        return entityTopic;
    }

    public void setEntityTopic(String entityTopic) {
        this.entityTopic = entityTopic;
    }

    /**
     * @Description 生成影子SRI
     * @return SRI
     * @author szh
     * @Date 2019/6/16 19:37
     */
    private String generateSRI() {
        return this.getClass().getSimpleName() + "_" +
                System.currentTimeMillis() + "_" +
                (int) (Math.random() * 1000);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShadowEntity)) {
            return false;
        }
        return this.SRI.equals(((ShadowEntity) o).getSRI());
    }

}
