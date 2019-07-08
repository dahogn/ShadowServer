package com.runhang.shadow.client.api;

import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.device.entity.CargoRoad;
import com.runhang.shadow.client.device.entity.Vending;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: wangzhendong
 * @Date: 2019/6/27 11:27
 * @Description:
 */
@RestController
public class WangApi {

    @GetMapping("/test")
    public String test(){
        Vending vending = (Vending) ShadowUtils.getShadow("test");
        // 开启三个线程，修改vendor的属性查看是否异常
        new Thread(() -> {
            try {
                List<CargoRoad> cargoRoadList = Collections.synchronizedList(vending.getCargoRoad());
                System.out.println(Thread.currentThread().getName()+" "+ cargoRoadList.size());
                if(cargoRoadList.size()>0){
                    //TODO 不加锁存在线程安全问题
                    TimeUnit.SECONDS.sleep(2);
                    cargoRoadList.remove(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "set" + 1).start();

        new Thread(() -> {
            try {
                List<CargoRoad> cargoRoadList = Collections.synchronizedList(vending.getCargoRoad());
                if(cargoRoadList.size()>0){
                    cargoRoadList.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, "set" + 2).start();


        new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName()+" "+ vending.getName());
                System.out.println(Thread.currentThread().getName()+" "+ vending.getCargoRoad().size());
                TimeUnit.SECONDS.sleep(2);
                System.out.println(Thread.currentThread().getName()+" "+ vending.getName());
                System.out.println(Thread.currentThread().getName()+" "+ vending.getCargoRoad().size());
                TimeUnit.SECONDS.sleep(4);
                System.out.println(Thread.currentThread().getName()+" "+ vending.getName());
                System.out.println(Thread.currentThread().getName()+" "+ vending.getCargoRoad().size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "set" + 3).start();

        return vending.toString();
    }

    @GetMapping("/semaphore")
    public String semaphore(){
        new Thread(() -> {
            Vending vending = (Vending) ShadowUtils.getShadow("test");
            //Vending vending2 = (Vending) ShadowUtils.getShadow("test");
            System.out.println(vending);
           // System.out.println(vending2);
           // System.out.println(vending.equals(vending2));

            List<CargoRoad> list = vending.getCargoRoad();
            System.out.println(Thread.currentThread().getName()+" 子类的长度 "+ list.size());
            if (list.size() > 0){
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list.remove(0);
            }
            System.out.println(Thread.currentThread().getName()+" 子类的长度 "+ list.size());
            ReErrorCode error = ShadowUtils.commitAndPush("test");
            if (null != error) {
                System.out.println(error.getErrorMsg());
            }
            Vending vending1 = (Vending) ShadowUtils.getShadow("test");
            System.out.println(Thread.currentThread().getName()+" 子类的长度 "+ vending1.getCargoRoad().size());
            ShadowUtils.commit("test");
            System.out.println(Thread.currentThread().getName() + "end");
        }, "修改线程" + 1).start();

        new Thread(() -> {
            Vending vending = (Vending) ShadowUtils.getShadow("test");
            if (vending != null){
                List<CargoRoad> list = vending.getCargoRoad();
                System.out.println(Thread.currentThread().getName()+" 子类的长度 "+ list.size());
                if (list.size() > 0){
                    list.remove(0);
                }
                System.out.println(Thread.currentThread().getName()+" 子类的长度 "+ list.size());
                ReErrorCode error = ShadowUtils.commitAndPush("test");
                if (null != error) {
                    System.out.println(error.getErrorMsg());
                }
            }
            System.out.println(Thread.currentThread().getName() + "end");
        }, "修改线程" + 2).start();

        return "success";
    }

}
