package com.marine.manage.Controller;

import com.marine.manage.pojo.Result;
import com.marine.manage.service.AsyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步任务控制器
 * 演示线程池在Web应用中的实际使用
 */
@Slf4j
@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncTaskController {

    private final AsyncTaskService asyncTaskService;

    /**
     * 发送邮件通知
     * 演示异步邮件发送，不阻塞主线程
     */
    @PostMapping("/send-email")
    public Result<String> sendEmail(@RequestParam String email,
                                   @RequestParam String subject,
                                   @RequestParam String content) {
        try {
            log.info("收到邮件发送请求 - 收件人: {}", email);

            // 异步发送邮件，立即返回
            CompletableFuture<String> future = asyncTaskService.sendEmailNotification(email, subject, content);

            // 可以选择等待结果或立即返回
            return Result.success("邮件发送任务已提交，正在后台处理");

        } catch (Exception e) {
            log.error("邮件发送请求处理失败", e);
            return Result.error("邮件发送请求失败: " + e.getMessage());
        }
    }

    /**
     * 同步等待邮件发送结果
     * 演示如何等待异步任务完成
     */
    @PostMapping("/send-email-sync")
    public Result<String> sendEmailSync(@RequestParam String email,
                                       @RequestParam String subject,
                                       @RequestParam String content) {
        try {
            log.info("收到同步邮件发送请求 - 收件人: {}", email);

            // 异步发送邮件并等待结果
            CompletableFuture<String> future = asyncTaskService.sendEmailNotification(email, subject, content);

            // 等待最多5秒
            String result = future.get(5, TimeUnit.SECONDS);

            return Result.success(result);

        } catch (TimeoutException e) {
            log.warn("邮件发送超时 - 收件人: {}", email);
            return Result.error("邮件发送超时，请稍后查看发送状态");
        } catch (ExecutionException e) {
            log.error("邮件发送执行失败 - 收件人: {}", email, e);
            return Result.error("邮件发送失败: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("邮件发送被中断 - 收件人: {}", email, e);
            return Result.error("邮件发送被中断");
        } catch (Exception e) {
            log.error("邮件发送请求处理失败", e);
            return Result.error("邮件发送请求失败: " + e.getMessage());
        }
    }

    /**
     * 生成课程统计报表
     * 演示CPU密集型任务的异步处理
     */
    @PostMapping("/lesson-statistics")
    public Result<String> generateLessonStatistics() {
        try {
            log.info("收到课程统计请求");

            // 异步处理统计任务
            CompletableFuture<String> future = asyncTaskService.processLessonStatistics();

            return Result.success("课程统计任务已启动，正在后台处理");

        } catch (Exception e) {
            log.error("课程统计请求处理失败", e);
            return Result.error("课程统计请求失败: " + e.getMessage());
        }
    }

    /**
     * 批量通知用户
     * 演示并发处理多个任务
     */
    @PostMapping("/batch-notify")
    public Result<String> batchNotifyUsers(@RequestBody List<String> emails,
                                          @RequestParam String message) {
        try {
            log.info("收到批量通知请求 - 用户数量: {}", emails.size());

            if (emails.isEmpty()) {
                return Result.error("用户邮箱列表不能为空");
            }

            // 异步批量通知
            CompletableFuture<String> future = asyncTaskService.batchNotifyUsers(emails, message);

            return Result.success("批量通知任务已启动，正在后台处理 " + emails.size() + " 个用户");

        } catch (Exception e) {
            log.error("批量通知请求处理失败", e);
            return Result.error("批量通知请求失败: " + e.getMessage());
        }
    }

    /**
     * 生成报表
     * 演示长时间运行任务的处理
     */
    @PostMapping("/generate-report")
    public Result<String> generateReport(@RequestParam String reportType) {
        try {
            log.info("收到报表生成请求 - 报表类型: {}", reportType);

            // 异步生成报表
            CompletableFuture<String> future = asyncTaskService.generateReport(reportType);

            return Result.success("报表生成任务已启动: " + reportType);

        } catch (Exception e) {
            log.error("报表生成请求处理失败", e);
            return Result.error("报表生成请求失败: " + e.getMessage());
        }
    }

    /**
     * 演示多个异步任务的并发执行
     * 同时处理邮件发送、统计计算和报表生成
     */
    @PostMapping("/concurrent-tasks")
    public Result<List<String>> executeConcurrentTasks(@RequestParam String email) {
        try {
            log.info("开始执行并发任务演示");

            // 同时启动多个异步任务
            CompletableFuture<String> emailTask = asyncTaskService.sendEmailNotification(
                email, "系统通知", "您的任务正在处理中");

            CompletableFuture<String> statisticsTask = asyncTaskService.processLessonStatistics();

            CompletableFuture<String> reportTask = asyncTaskService.generateReport("月度报表");

            // 创建组合任务，等待所有任务完成
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                emailTask, statisticsTask, reportTask);

            // 等待所有任务完成（最多15秒）
            allTasks.get(15, TimeUnit.SECONDS);

            // 收集所有任务结果
            List<String> results = new ArrayList<>();
            results.add("邮件任务: " + emailTask.get());
            results.add("统计任务: " + statisticsTask.get());
            results.add("报表任务: " + reportTask.get());

            log.info("所有并发任务执行完成");
            return Result.success(results);

        } catch (TimeoutException e) {
            log.warn("并发任务执行超时");
            return Result.error("任务执行超时，部分任务可能仍在后台运行");
        } catch (Exception e) {
            log.error("并发任务执行失败", e);
            return Result.error("并发任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取线程池状态信息
     * 用于监控线程池的使用情况
     */
    @GetMapping("/thread-pool-status")
    public Result<String> getThreadPoolStatus() {
        try {
            // 实际项目中可以通过JMX或其他方式获取线程池状态
            String status = String.format(
                "线程池状态监控 - 当前时间: %s, 活跃线程: %d",
                java.time.LocalDateTime.now().toString(),
                Thread.activeCount()
            );

            log.info("线程池状态查询: {}", status);
            return Result.success(status);

        } catch (Exception e) {
            log.error("获取线程池状态失败", e);
            return Result.error("获取线程池状态失败: " + e.getMessage());
        }
    }
}
