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

    private String name;

    public void setName(String name) {
        /* 上写锁 */
        lock.writeLock().lock();
        try{
            this.name = name;
            EntityField field = new EntityField("Vending", "name", name);
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
        this.topic = topic;
        EntityField field = new EntityField("Vending", "topic", topic);
        notifyObservers(databaseFieldMap.get("topic"), field);
    }

    public String getTopic() {
        return topic;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "vending_id")
    private List<CargoRoad> cargoRoad;

    public void setCargoRoad(List<CargoRoad> cargoRoad) {
        this.cargoRoad = cargoRoad;
        EntityField field = new EntityField("Vending", "cargoRoad", cargoRoad);
        notifyObservers(databaseFieldMap.get("cargoRoad"), field);
    }

    public List<CargoRoad> getCargoRoad() {
        return cargoRoad;
    }

}