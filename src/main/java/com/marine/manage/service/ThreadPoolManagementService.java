package com.marine.manage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池管理服务
 * 提供线程池状态监控和管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadPoolManagementService {

    @Qualifier("asyncTaskExecutor")
    private final Executor asyncTaskExecutor;

    @Qualifier("emailTaskExecutor")
    private final Executor emailTaskExecutor;

    @Qualifier("dataProcessExecutor")
    private final Executor dataProcessExecutor;

    /**
     * 获取所有线程池的状态信息
     */
    public Map<String, Map<String, Object>> getAllThreadPoolStatus() {
        Map<String, Map<String, Object>> statusMap = new HashMap<>();

        statusMap.put("asyncTaskExecutor", getThreadPoolStatus((ThreadPoolTaskExecutor) asyncTaskExecutor));
        statusMap.put("emailTaskExecutor", getThreadPoolStatus((ThreadPoolTaskExecutor) emailTaskExecutor));
        statusMap.put("dataProcessExecutor", getThreadPoolStatus((ThreadPoolTaskExecutor) dataProcessExecutor));

        return statusMap;
    }

    /**
     * 获取单个线程池的详细状态
     */
    private Map<String, Object> getThreadPoolStatus(ThreadPoolTaskExecutor executor) {
        Map<String, Object> status = new HashMap<>();

        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();

        status.put("corePoolSize", threadPool.getCorePoolSize());
        status.put("maximumPoolSize", threadPool.getMaximumPoolSize());
        status.put("currentPoolSize", threadPool.getPoolSize());
        status.put("activeCount", threadPool.getActiveCount());
        status.put("completedTaskCount", threadPool.getCompletedTaskCount());
        status.put("taskCount", threadPool.getTaskCount());
        status.put("queueSize", threadPool.getQueue().size());
        status.put("queueRemainingCapacity", threadPool.getQueue().remainingCapacity());
        status.put("isShutdown", threadPool.isShutdown());
        status.put("isTerminated", threadPool.isTerminated());

        // 计算负载率
        double loadPercentage = (double) threadPool.getActiveCount() / threadPool.getMaximumPoolSize() * 100;
        status.put("loadPercentage", String.format("%.2f%%", loadPercentage));

        // 计算队列使用率
        int queueCapacity = threadPool.getQueue().size() + threadPool.getQueue().remainingCapacity();
        double queueUsagePercentage = queueCapacity > 0 ?
            (double) threadPool.getQueue().size() / queueCapacity * 100 : 0;
        status.put("queueUsagePercentage", String.format("%.2f%%", queueUsagePercentage));

        return status;
    }

    /**
     * 获取线程池健康状态
     */
    public Map<String, String> getThreadPoolHealth() {
        Map<String, String> healthMap = new HashMap<>();

        healthMap.put("asyncTaskExecutor", evaluateThreadPoolHealth((ThreadPoolTaskExecutor) asyncTaskExecutor));
        healthMap.put("emailTaskExecutor", evaluateThreadPoolHealth((ThreadPoolTaskExecutor) emailTaskExecutor));
        healthMap.put("dataProcessExecutor", evaluateThreadPoolHealth((ThreadPoolTaskExecutor) dataProcessExecutor));

        return healthMap;
    }

    /**
     * 评估线程池健康状态
     */
    private String evaluateThreadPoolHealth(ThreadPoolTaskExecutor executor) {
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();

        if (threadPool.isShutdown()) {
            return "SHUTDOWN";
        }

        double loadPercentage = (double) threadPool.getActiveCount() / threadPool.getMaximumPoolSize() * 100;
        int queueCapacity = threadPool.getQueue().size() + threadPool.getQueue().remainingCapacity();
        double queueUsagePercentage = queueCapacity > 0 ?
            (double) threadPool.getQueue().size() / queueCapacity * 100 : 0;

        if (loadPercentage > 90 || queueUsagePercentage > 80) {
            return "CRITICAL";
        } else if (loadPercentage > 70 || queueUsagePercentage > 60) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * 动态调整线程池大小
     */
    public boolean adjustThreadPoolSize(String poolName, int coreSize, int maxSize) {
        try {
            ThreadPoolTaskExecutor executor = getExecutorByName(poolName);
            if (executor != null) {
                ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();

                // 记录调整前的状态
                log.info("调整线程池 {} - 当前核心大小: {}, 最大大小: {}",
                        poolName, threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize());

                // 调整线程池大小
                threadPool.setCorePoolSize(coreSize);
                threadPool.setMaximumPoolSize(maxSize);

                log.info("线程池 {} 调整完成 - 新核心大小: {}, 新最大大小: {}",
                        poolName, coreSize, maxSize);

                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("调整线程池大小失败: {}", poolName, e);
            return false;
        }
    }

    /**
     * 根据名称获取执行器
     */
    private ThreadPoolTaskExecutor getExecutorByName(String poolName) {
        switch (poolName) {
            case "asyncTaskExecutor":
                return (ThreadPoolTaskExecutor) asyncTaskExecutor;
            case "emailTaskExecutor":
                return (ThreadPoolTaskExecutor) emailTaskExecutor;
            case "dataProcessExecutor":
                return (ThreadPoolTaskExecutor) dataProcessExecutor;
            default:
                return null;
        }
    }
}
