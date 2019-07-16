package com.runhang.shadow.client.core.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @ClassName ShadowServiceAspect
 * @Description 影子服务切面
 * @Date 2019/7/16 13:06
 * @author szh
 **/
@Component
@Slf4j
@Aspect
public class ShadowServiceAspect {

    @After("@annotation(com.runhang.shadow.client.core.aspect.ShadowService)")
    public void dealShadowService(JoinPoint point) {
        log.warn("deal shadow service");
        log.info("aspect thread: " + Thread.currentThread().getName());
    }

}
