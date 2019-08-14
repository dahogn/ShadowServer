package com.runhang.shadow.client.demo.entity.param;

import com.runhang.shadow.client.device.entity.Commodity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CommodityParam
 * @Description 商品入参
 * @Date 2019/8/14 10:54
 * @author szh
 **/
@Data
public class CommodityParam {

    private String cargoRoadId;
    private List<CommodityInfoParam> commodityList;

    public List<Commodity> getCommodityList(String topic) {
        List<Commodity> commodities = new ArrayList<>();
        if (null != commodityList) {
            for (CommodityInfoParam param : commodityList) {
                Commodity commodity = param.getCommodity(topic);
                commodities.add(commodity);
            }
        }
        return commodities;
    }

}
