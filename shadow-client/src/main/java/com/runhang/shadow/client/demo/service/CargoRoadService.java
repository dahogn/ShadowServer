package com.runhang.shadow.client.demo.service;

import com.runhang.shadow.client.core.aspect.ShadowService;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.demo.entity.param.CargoRoadParam;
import com.runhang.shadow.client.demo.entity.result.CargoRoadSimpleInfo;
import com.runhang.shadow.client.device.entity.CargoRoad;
import com.runhang.shadow.client.device.entity.Vending;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CargoRoadService
 * @Description 货道service
 * @Date 2019/8/12 15:04
 * @author szh
 **/
@Service
@Slf4j
public class CargoRoadService {

    /**
     * @Description 获取售货机的货道
     * @param vendingId 售货机sri
     * @return 货道列表
     * @author szh
     * @Date 2019/8/12 15:31
     */
    @ShadowService
    public List<CargoRoadSimpleInfo> getCargoRoadList(String vendingId) {
        List<CargoRoadSimpleInfo> cargoRoadList = new ArrayList<>();
        Vending vending = (Vending) ShadowUtils.getEntity(vendingId);
        if (null != vending) {
            for (CargoRoad cargoRoad : vending.getCargoRoad()) {
                CargoRoadSimpleInfo info = new CargoRoadSimpleInfo(cargoRoad);
                cargoRoadList.add(info);
            }
        }
        return cargoRoadList;
    }
    
    /**
     * @Description 新增货道
     * @param param 货道详情
     * @author szh
     * @Date 2019/8/13 20:50       
     */
    @ShadowService
    public String addCargoRoad(CargoRoadParam param) {
        Vending vending = (Vending) ShadowUtils.getEntity(param.getVendingSri());
        CargoRoad cargoRoad = param.getCargoRoad(vending.getTopic());
        if (null != vending.getCargoRoad()) {
            vending.getCargoRoad().add(cargoRoad);
        } else {
            List<CargoRoad> cargoRoadList = new ArrayList<>();
            vending.setCargoRoad(cargoRoadList);
        }

        try {
            ShadowUtils.commitAndPush(vending.getTopic());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "success";
    }

}
