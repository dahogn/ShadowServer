package com.runhang.shadow.client.device.entity;

import com.runhang.shadow.client.core.model.DatabaseField;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Commodity extends ShadowEntity {

    static {
        databaseFieldMap = new HashMap<>();
        databaseFieldMap.put("number", new DatabaseField("re_road_commodity", "number"));
        databaseFieldMap.put("price", new DatabaseField("commodity", "price"));
        databaseFieldMap.put("name", new DatabaseField("commodity", "name"));
        databaseFieldMap.put("id", new DatabaseField("commodity", "id"));
    }

    private Integer number;

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }

    private double price;

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}