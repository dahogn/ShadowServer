package com.runhang.shadow.client.device.entity;

import com.runhang.shadow.client.core.model.DatabaseField;
import com.runhang.shadow.client.core.model.EntityField;

import javax.persistence.*;
import java.util.*;

@Entity
public class Vending extends ShadowEntity {

    @Transient
    private static Map<String, DatabaseField> databaseFieldMap = new HashMap<>();

    static {
        databaseFieldMap.put("name", new DatabaseField("vending", "name"));
        databaseFieldMap.put("id", new DatabaseField("vending", "id"));
    }

    public Vending() {

    }

    public Vending(String topic) throws Exception {
        super(topic);
    }

    private String name;

    public void setName(String name) {
        /* 上写锁 */
        lock.writeLock().lock();
        try{
            EntityField field = new EntityField("Vending", "name", this.name);
            this.name = name;
            field.setFieldValue(name);
            notifyObservers(databaseFieldMap.get("name"), field);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.writeLock().unlock();
        }
    }

    public String getName() {
        /* 上读锁 */
        lock.readLock().lock();
        try {
            return name;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    private String topic;

    public void setTopic(String topic) {
        EntityField field = new EntityField("Vending", "topic", this.topic);
        this.topic = topic;
        field.setFieldValue(topic);
        notifyObservers(databaseFieldMap.get("topic"), field);
    }

    public String getTopic() {
        return topic;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "vending_id")
    private List<CargoRoad> cargoRoad;

    public void setCargoRoad(List<CargoRoad> cargoRoad) {
        EntityField field = new EntityField("Vending", "cargoRoad", this.cargoRoad);
        this.cargoRoad = cargoRoad;
        field.setFieldValue(field);
        notifyObservers(databaseFieldMap.get("cargoRoad"), field);
    }

    public List<CargoRoad> getCargoRoad() {
        return cargoRoad;
    }

}