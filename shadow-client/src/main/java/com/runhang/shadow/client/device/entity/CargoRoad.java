package com.runhang.shadow.client.device.entity;

import com.runhang.shadow.client.core.model.DatabaseField;
import com.runhang.shadow.client.core.shadow.ShadowEntity;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class CargoRoad extends ShadowEntity {

    @Transient
    private static Map<String, DatabaseField> databaseFieldMap = new HashMap<>();

    static {
        databaseFieldMap.put("serial", new DatabaseField("cargo_road", "serial"));
        databaseFieldMap.put("id", new DatabaseField("cargo_road", "id"));
    }

    public CargoRoad() {
        super();
    }

    public CargoRoad(String topic) throws Exception {
        super(topic);
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "cargo_road_id")
    private List<Commodity> commodity;

    public void setCommodity(List<Commodity> commodity) {
        this.commodity = commodity;
    }

    public List<Commodity> getCommodity() {
        return commodity;
    }

    private Integer serial;

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public Integer getSerial() {
        return serial;
    }

}