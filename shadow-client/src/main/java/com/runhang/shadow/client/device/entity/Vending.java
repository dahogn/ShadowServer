package com.runhang.shadow.client.device.entity;

import com.runhang.shadow.client.core.databaseSync.ShadowSubject;
import com.runhang.shadow.client.core.model.DatabaseField;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Vending extends ShadowSubject {

@Transient
public static Map<String, DatabaseField> databaseFieldMap;
static {
databaseFieldMap = new HashMap<>();
databaseFieldMap.put("name", new DatabaseField("vending", "name"));
databaseFieldMap.put("id", new DatabaseField("vending", "id"));
}

@Id
@GeneratedValue
private int id;
public void setId(int id) { this.id = id; }
public int getId() { return id; }

private String name;
public void setName(String name) { this.name = name; }
public String getName() { return name; }

private String topic;
public void setTopic(String topic) { this.topic = topic; }
public String getTopic() { return topic; }

@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
@JoinColumn(name = "cargo_road_id")
private List<CargoRoad> cargoRoad;
public void setCargoRoad(List<CargoRoad> cargoRoad) { this.cargoRoad = cargoRoad; }
public List<CargoRoad> getCargoRoad() { return cargoRoad; }

}