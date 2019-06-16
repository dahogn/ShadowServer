package com.runhang.shadow.client.device.entity;

import com.runhang.shadow.client.core.shadow.ShadowFactory;
import com.runhang.shadow.client.core.shadow.ShadowSubject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
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

    @Id
    @GeneratedValue
    private int id;

    /**
     * Shadow Resource Identifier
     * 影子资源标识符
     */
    @Column(name = "sri")
    private String SRI;

    /**
     * 重入读写锁
     */
    @Transient
    ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * @Description 初始化生成SRI并注入容器
     * @author szh
     * @Date 2019/6/16 19:54
     */
    ShadowEntity() {
        super();
        this.SRI = generateSRI();
        boolean injectRe = ShadowFactory.injectEntity(this);
        log.info("inject " + SRI + ": " + injectRe);
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

}
