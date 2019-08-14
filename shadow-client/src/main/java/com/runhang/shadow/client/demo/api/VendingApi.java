package com.runhang.shadow.client.demo.api;

import com.alibaba.fastjson.JSON;
import com.runhang.shadow.client.demo.entity.param.CargoRoadParam;
import com.runhang.shadow.client.demo.entity.param.CommodityParam;
import com.runhang.shadow.client.demo.service.CargoRoadService;
import com.runhang.shadow.client.demo.service.CommodityService;
import com.runhang.shadow.client.demo.service.VendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName VendingApi
 * @Description 售货机接口
 * @Date 2019/8/8 12:49
 * @author szh
 **/
@Slf4j
@Controller
@RequestMapping("demo")
public class VendingApi {

    @Autowired
    private VendingService vendingService;
    @Autowired
    private CargoRoadService cargoRoadService;
    @Autowired
    private CommodityService commodityService;

    /**
     * @Description 获取所有售货机
     * @author szh
     * @Date 2019/8/12 15:30
     */
    @GetMapping("vending")
    @ResponseBody
    public String getVendingList() {
        return JSON.toJSONString(vendingService.getVendingList());
    }

    /**
     * @Description 获取售货机的货道列表
     * @param vendingId 售货机sri
     * @author szh
     * @Date 2019/8/12 15:30
     */
    @GetMapping("cargoRoad")
    @ResponseBody
    public String getCargoRoadList(@RequestParam("vendingId") String vendingId) {
        return JSON.toJSONString(cargoRoadService.getCargoRoadList(vendingId));
    }

    /**
     * @Description 新增货道
     * @param param 货道详情
     * @author szh
     * @Date 2019/8/13 20:50       
     */
    @PostMapping("cargoRoad")
    @ResponseBody
    public String addCargoRoad(@RequestBody CargoRoadParam param) {
        return cargoRoadService.addCargoRoad(param);
    }

    /**
     * @Description 获取商品列表
     * @param cargoRoadId 货道sri
     * @author szh
     * @Date 2019/8/12 17:11
     */
    @GetMapping("commodity")
    @ResponseBody
    public String getCommodityList(@RequestParam("cargoRoadId") String cargoRoadId) {
        return JSON.toJSONString(commodityService.getCommodityList(cargoRoadId));
    }

    /**
     * @Description 修改商品列表
     * @param param 新的商品列表
     * @author szh
     * @Date 2019/8/14 11:14
     */
    @PutMapping("commodity")
    @ResponseBody
    public String editCommodityList(@RequestBody CommodityParam param) {
        return commodityService.editCommodityList(param);
    }

}
