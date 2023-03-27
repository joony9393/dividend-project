package com.dayone.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();
        int n = Runtime.getRuntime().availableProcessors(); // 코어 개수
        threadPool.setPoolSize(n);  // 스레드의 개수 설정
        threadPool.initialize();
        taskRegistrar.setTaskScheduler(threadPool); // 스케줄러에서 방금 생성한 스레드 풀을 사용한다.
    }
}
