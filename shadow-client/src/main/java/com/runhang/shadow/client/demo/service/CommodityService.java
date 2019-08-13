package com.runhang.shadow.client.demo.service;

import com.runhang.shadow.client.core.aspect.ShadowService;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.demo.entity.result.CommoditySimpleInfo;
import com.runhang.shadow.client.device.entity.CargoRoad;
import com.runhang.shadow.client.device.entity.Commodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CommodityService
 * @Description 商品service
 * @Date 2019/8/12 17:13
 * @author szh
 **/
@Service
@Slf4j
public class CommodityService {

    /**
     * @Description 获取货道内商品
     * @param cargoRoadId 货道sri
     * @return 商品列表
     * @author szh
     * @Date 2019/8/12 17:18
     */
    @ShadowService
    public List<CommoditySimpleInfo> getCommodityList(String cargoRoadId) {
        List<CommoditySimpleInfo> commodityList = new ArrayList<>();
        CargoRoad cargoRoad = (CargoRoad) ShadowUtils.getEntity(cargoRoadId);

        if (null != cargoRoad) {
            for (Commodity commodity : cargoRoad.getCommodity()) {
                CommoditySimpleInfo info = new CommoditySimpleInfo(commodity);
                commodityList.add(info);
            }
        }

        return commodityList;
    }

}
