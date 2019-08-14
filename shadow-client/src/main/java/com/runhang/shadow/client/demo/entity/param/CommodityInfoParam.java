package com.runhang.shadow.client.demo.entity.param;

import com.runhang.shadow.client.device.entity.Commodity;
import lombok.Data;

/**
 * @ClassName CommodityInfoParam
 * @Description 商品详情
 * @Date 2019/8/14 10:55
 * @author szh
 **/
@Data
public class CommodityInfoParam {

    private String name;
    private int num;
    private double price;
    private String sri;

    public Commodity getCommodity(String topic) {
        Commodity commodity = new Commodity();
        commodity.setName(name);
        commodity.setNumber(num);
        commodity.setPrice(price);
        commodity.setSRI(sri);
        commodity.setEntityTopic(topic);
        return commodity;
    }

}
