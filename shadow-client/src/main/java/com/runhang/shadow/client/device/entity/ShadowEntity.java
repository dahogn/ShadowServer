package com.runhang.shadow.client.device.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.runhang.shadow.client.core.shadow.EntityFactory;
import com.runhang.shadow.client.core.shadow.ShadowSubject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
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

    /**
     * Shadow Resource Identifier
     * 影子资源标识符
     */
    @Id
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
        generateSRI();
    }

    /**
     * @Description 初始化生成SRI并注入容器
     * @author szh
     * @Date 2019/6/16 19:54
     */
    public ShadowEntity(String topic) throws Exception {
        super();
        generateSRI();
        this.entityTopic =  topic;
        EntityFactory.injectEntity(this);
        //log.info("inject " + SRI + ": " + injectRe);
    }

    /**
     * @Description 生成影子SRI
     * @author szh
     * @Date 2019/6/16 19:37
     */
    public void generateSRI() {
        int random = (int) (Math.random() * 1000);
        this.SRI = this.getClass().getSimpleName() + "_" +
                System.currentTimeMillis() + "_" +
                String.format("%03d", random);
    }

    /**
     * @Description 通过sri判断是否是同一个实体
     * @param o 另一个实体
     * @return 是否是同一个
     * @author szh
     * @Date 2019/7/18 9:44
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShadowEntity)) {
            return false;
        }
        return this.SRI.equals(((ShadowEntity) o).getSRI());
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

}
