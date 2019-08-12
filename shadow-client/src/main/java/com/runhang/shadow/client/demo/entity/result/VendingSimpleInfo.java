package com.runhang.shadow.client.demo.entity.result;

import com.runhang.shadow.client.device.entity.Vending;
import lombok.Data;

/**
 * @ClassName VendingSimpleInfo
 * @Description 售货机简略信息
 * @Date 2019/8/9 23:31
 * @author szh
 **/
@Data
public class VendingSimpleInfo {

    private String name;
    private String topic;
    private String sri;
    private int cargoRoadNum;

    public VendingSimpleInfo() {

    }

    public VendingSimpleInfo(Vending vending) {
        this.name = vending.getName();
        this.topic = vending.getTopic();
        this.sri = vending.getSRI();
        this.cargoRoadNum = vending.getCargoRoad().size();
    }

}
