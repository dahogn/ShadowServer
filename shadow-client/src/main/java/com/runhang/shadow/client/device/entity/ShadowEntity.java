package com.runhang.shadow.client.device.entity;

import com.runhang.shadow.client.core.model.DatabaseField;
import com.runhang.shadow.client.core.shadow.ShadowFactory;
import com.runhang.shadow.client.core.shadow.ShadowSubject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * @ClassName ShadowEntity
 * @Description 影子实体
 * @Date 2019/6/16 16:49
 * @author szh
 **/
@Slf4j
@MappedSuperclass
public class ShadowEntity extends ShadowSubject implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Shadow Resource Identifier
     * 影子资源标识符
     */
    @Column(name = "sri")
    private String SRI;

    /**
     * 实体所属的影子对象的topic
     */
    @Transient
    private String entityTopic;

    /**
     * 数据库字段映射关系
     */
    @Transient
    public static Map<String, DatabaseField> databaseFieldMap;

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
        boolean injectRe = ShadowFactory.injectEntity(this);
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
