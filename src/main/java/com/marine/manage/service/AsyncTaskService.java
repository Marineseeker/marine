package com.marine.manage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 异步服务类
 * 演示线程池在各种业务场景中的使用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

  private final LessonService lessonService;

  /**
   * 异步发送邮件通知
   * 使用邮件专用线程池
   */
  @Async("emailTaskExecutor")
  public CompletableFuture<String> sendEmailNotification(String email, String subject, String content) {
    try {
      log.info("开始发送邮件到: {} - 线程: {}", email, Thread.currentThread().getName());

      // 模拟邮件发送耗时操作
      TimeUnit.SECONDS.sleep(2);

      log.info("邮件发送成功: {} - 线程: {}", email, Thread.currentThread().getName());
      return CompletableFuture.completedFuture("邮件发送成功");

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("邮件发送被中断: {}", email, e);
      return CompletableFuture.completedFuture("邮件发送失败");
    } catch (Exception e) {
      log.error("邮件发送失败: {}", email, e);
      return CompletableFuture.completedFuture("邮件发送失败: " + e.getMessage());
    }
  }

  /**
   * 异步处理课程数据统计
   * 使用数据处理专用线程池
   */
  @Async("dataProcessExecutor")
  public CompletableFuture<String> processLessonStatistics() {
    try {
      log.info("开始处理课程统计数据 - 线程: {}", Thread.currentThread().getName());

      // 模拟CPU密集型计算
      long startTime = System.currentTimeMillis();

      // 获取所有课程数据
      var lessons = lessonService.getAllLessons();

      // 模拟复杂的数据处理
      int totalLessons = lessons.size();
      double averageProcessingTime = calculateAverageProcessingTime(lessons);

      long endTime = System.currentTimeMillis();

      String result = String.format("课程统计完成 - 总课程数: %d, 平均处理时间: %.2f秒, 统计耗时: %d毫秒",
              totalLessons, averageProcessingTime, (endTime - startTime));

      log.info("{} - 线程: {}", result, Thread.currentThread().getName());
      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      log.error("课程统计处理失败", e);
      return CompletableFuture.completedFuture("统计处理失败: " + e.getMessage());
    }
  }

  /**
   * 异步批量处理用户通知
   * 使用通用异步线程池
   */
  @Async("asyncTaskExecutor")
  public CompletableFuture<String> batchNotifyUsers(List<String> userEmails, String message) {
    try {
      log.info("开始批量通知用户，用户数量: {} - 线程: {}", userEmails.size(), Thread.currentThread().getName());

      int successCount = 0;
      int failCount = 0;

      for (String email : userEmails) {
        try {
          // 模拟发送通知
          TimeUnit.MILLISECONDS.sleep(100);

          // 模拟90%成功率
          if (Math.random() > 0.1) {
            successCount++;
            log.debug("通知发送成功: {}", email);
          } else {
            failCount++;
            log.warn("通知发送失败: {}", email);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          failCount++;
          break;
        }
      }

      String result = String.format("批量通知完成 - 成功: %d, 失败: %d", successCount, failCount);
      log.info("{} - 线程: {}", result, Thread.currentThread().getName());

      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      log.error("批量通知处理失败", e);
      return CompletableFuture.completedFuture("批量通知失败: " + e.getMessage());
    }
  }

  /**
   * 异步生成报表
   * 演示长时间运行的任务
   */
  @Async("dataProcessExecutor")
  public CompletableFuture<String> generateReport(String reportType) {
    try {
      log.info("开始生成报表: {} - 线程: {}", reportType, Thread.currentThread().getName());

      // 模拟报表生成的各个阶段
      updateProgress(reportType, "数据收集中...", 20);
      TimeUnit.SECONDS.sleep(1);

      updateProgress(reportType, "数据处理中...", 50);
      TimeUnit.SECONDS.sleep(2);

      updateProgress(reportType, "报表生成中...", 80);
      TimeUnit.SECONDS.sleep(1);

      updateProgress(reportType, "报表完成", 100);

      String result = "报表生成完成: " + reportType;
      log.info("{} - 线程: {}", result, Thread.currentThread().getName());

      return CompletableFuture.completedFuture(result);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("报表生成被中断: {}", reportType, e);
      return CompletableFuture.completedFuture("报表生成被中断");
    } catch (Exception e) {
      log.error("报表生成失败: {}", reportType, e);
      return CompletableFuture.completedFuture("报表生成失败: " + e.getMessage());
    }
  }

  /**
   * 模拟计算平均处理时间
   */
  private double calculateAverageProcessingTime(List<?> data) {
    // 模拟CPU密集型计算
    long sum = 0;
    for (int i = 0; i < data.size() * 1000; i++) {
      sum += Math.sqrt(i);
    }
    return sum / 1000000.0;
  }

  /**
   * 更新进度（实际项目中可能会更新到缓存或数据库）
   */
  private void updateProgress(String taskId, String message, int progress) {
    log.info("任务进度更新 - 任务ID: {}, 消息: {}, 进度: {}%", taskId, message, progress);
  }
}
