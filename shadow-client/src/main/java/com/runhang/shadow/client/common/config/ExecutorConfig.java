package com.runhang.shadow.client.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName ExecutorConfig
 * @Description 线程池配置
 * @Date 2019/7/3 16:22
 * @author szh
 **/
@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean("asyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        log.error("start asyncServiceExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(5);
        // 队列大小
        executor.setQueueCapacity(99999);
        // 允许线程空闲时间
        executor.setKeepAliveSeconds(60);
        // 线程池中的线程的名称前缀
        executor.setThreadNamePrefix("async-service-");
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 执行初始化
        executor.initialize();
        return executor;
    }

}
