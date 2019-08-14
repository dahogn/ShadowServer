package com.runhang.shadow.client.demo.entity.result;

import com.runhang.shadow.client.device.entity.CargoRoad;
import com.runhang.shadow.client.device.entity.Commodity;
import lombok.Data;

/**
 * @ClassName CargoRoadSimpleInfo
 * @Description 货道简略信息
 * @Date 2019/8/12 15:12
 * @author szh
 **/
@Data
public class CargoRoadSimpleInfo {

    private String sri;
    private int serial;
    private int commodityNum;

    public CargoRoadSimpleInfo() {

    }

    public CargoRoadSimpleInfo(CargoRoad cargoRoad) {
        this.sri = cargoRoad.getSRI();
        this.serial = cargoRoad.getSerial();
        int num = 0;
        if (null != cargoRoad.getCommodity()) {
            for (Commodity commodity : cargoRoad.getCommodity()) {
                num += commodity.getNumber();
            }
        }
        this.commodityNum = num;
    }

}
