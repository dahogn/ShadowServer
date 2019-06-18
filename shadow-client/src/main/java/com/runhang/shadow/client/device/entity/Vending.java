package com.runhang.shadow.client.device.entity;

import com.runhang.shadow.client.core.model.DatabaseField;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Vending extends ShadowEntity {

    static {
        databaseFieldMap = new HashMap<>();
        databaseFieldMap.put("name", new DatabaseField("vending", "name"));
        databaseFieldMap.put("id", new DatabaseField("vending", "id"));
    }

    private String name;

    public void setName(String name) {
        rwLock.writeLock().lock();
        this.name = name;
        rwLock.writeLock().unlock();
    }

    public String getName() {
        return name;
    }

    private String topic;

    public void setTopic(String topic) {
        rwLock.writeLock().lock();
        this.topic = topic;
        rwLock.writeLock().unlock();
    }

    public String getTopic() {
        return topic;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "vending_id")
    private List<CargoRoad> cargoRoad;

    public void setCargoRoad(List<CargoRoad> cargoRoad) {
        rwLock.writeLock().lock();
        this.cargoRoad = cargoRoad;
        rwLock.writeLock().unlock();
    }

    public List<CargoRoad> getCargoRoad() {
        return cargoRoad;
    }

}