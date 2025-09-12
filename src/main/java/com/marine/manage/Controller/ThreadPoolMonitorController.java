package com.marine.manage.Controller;

import com.marine.manage.pojo.Result;
import com.marine.manage.service.ThreadPoolManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 线程池监控控制器
 * 提供线程池状态监控和管理的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/threadpool")
@RequiredArgsConstructor
public class ThreadPoolMonitorController {

    private final ThreadPoolManagementService threadPoolManagementService;

    /**
     * 获取所有线程池的状态信息
     */
    @GetMapping("/status")
    public Result<Map<String, Map<String, Object>>> getThreadPoolStatus() {
        try {
            Map<String, Map<String, Object>> status = threadPoolManagementService.getAllThreadPoolStatus();
            log.info("获取线程池状态信息成功");
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取线程池状态失败", e);
            return Result.error("获取线程池状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取线程池健康状态
     */
    @GetMapping("/health")
    public Result<Map<String, String>> getThreadPoolHealth() {
        try {
            Map<String, String> health = threadPoolManagementService.getThreadPoolHealth();
            log.info("获取线程池健康状态成功");
            return Result.success(health);
        } catch (Exception e) {
            log.error("获取线程池健康状态失败", e);
            return Result.error("获取线程池健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 动态调整线程池大小
     */
    @PostMapping("/adjust")
    public Result<String> adjustThreadPool(@RequestParam String poolName,
                                          @RequestParam int coreSize,
                                          @RequestParam int maxSize) {
        try {
            if (coreSize <= 0 || maxSize <= 0 || coreSize > maxSize) {
                return Result.error("线程池参数无效：核心大小和最大大小必须大于0，且核心大小不能大于最大大小");
            }

            boolean success = threadPoolManagementService.adjustThreadPoolSize(poolName, coreSize, maxSize);

            if (success) {
                String message = String.format("线程池 %s 调整成功 - 核心大小: %d, 最大大小: %d",
                        poolName, coreSize, maxSize);
                log.info(message);
                return Result.success(message);
            } else {
                return Result.error("线程池调整失败：未找到指定的线程池 " + poolName);
            }
        } catch (Exception e) {
            log.error("调整线程池失败", e);
            return Result.error("调整线程池失败: " + e.getMessage());
        }
    }
}
