package com.runhang.shadow.client.demo.entity.param;

import com.runhang.shadow.client.device.entity.CargoRoad;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName CargoRoadParam
 * @Description 货道入参
 * @Date 2019/8/13 16:04
 * @author szh
 **/
@Data
@Slf4j
public class CargoRoadParam {

    private String vendingSri;
    private int serial;

    public CargoRoad getCargoRoad(String topic) {
        try {
            CargoRoad cargoRoad = new CargoRoad(topic);
            cargoRoad.setSerial(serial);
            return cargoRoad;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

}
