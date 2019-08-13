package com.runhang.shadow.client.demo.entity.result;

import com.runhang.shadow.client.device.entity.Commodity;
import lombok.Data;

/**
 * @ClassName CommoditySimpleInfo
 * @Description 商品简略信息
 * @Date 2019/8/12 17:14
 * @author szh
 **/
@Data
public class CommoditySimpleInfo {

    private String sri;
    private String name;
    private int num;
    private double price;

    public CommoditySimpleInfo() {

    }

    public CommoditySimpleInfo(Commodity commodity) {
        this.sri = commodity.getSRI();
        this.name = commodity.getName();
        this.num = commodity.getNumber();
        this.price = commodity.getPrice();
    }

}
