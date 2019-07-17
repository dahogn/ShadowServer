package com.runhang.shadow.client.core.aspect;

import com.runhang.shadow.client.core.shadow.ShadowFactory;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

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

    /**
     * @Description 影子服务完成后释放信号量
     * @param point 连接点
     * @author szh
     * @Date 2019/7/16 18:05
     */
    @After("@annotation(com.runhang.shadow.client.core.aspect.ShadowService)")
    public void dealShadowService(JoinPoint point) {
        log.warn("deal shadow service");
        log.info("aspect thread: " + Thread.currentThread().getName());
        String threadName = Thread.currentThread().getName();
        List<String> topics = ShadowFactory.getThreadTopic(threadName);
        if (topics != null){
            for (String topic: topics){
                ShadowUtils.releaseSemaphore(topic);
            }
        }
    }

    /**
     * @Description 影子服务抛出异常进行回滚
     * @param point 连接点
     * @param e 异常
     * @author szh
     * @Date 2019/7/16 18:06
     */
    @AfterThrowing(value = "@annotation(com.runhang.shadow.client.core.aspect.ShadowService)", throwing = "e")
    public void dealServiceException(JoinPoint point, Exception e) throws Throwable {
        throw e;
    }

}
