package com.runhang.shadow.client.demo.api;

import com.alibaba.fastjson.JSON;
import com.runhang.shadow.client.demo.service.CargoRoadService;
import com.runhang.shadow.client.demo.service.VendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
