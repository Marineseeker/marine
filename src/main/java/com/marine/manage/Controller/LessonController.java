package com.marine.manage.Controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.marine.manage.annotaion.TrackTime;
import com.marine.manage.pojo.Lesson;
import com.marine.manage.pojo.Result;
import com.marine.manage.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    private final RedisTemplate<String, Object> redisTemplate;

    @SaCheckRole("admin")
    @GetMapping("/getAllLessons")
    public Result<List<Lesson>> getAllLessons() {
        try {
            List<Lesson> lessons = lessonService.getAllLessons();
            return Result.success(lessons);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @TrackTime
    @GetMapping("/mylessons")
    public Result<List<Lesson>> getMyLessons() {
        if (!StpUtil.isLogin()) {
            return Result.error("未登录或token无效！");
        }
        int userId = StpUtil.getLoginIdAsInt();
        String redisKey = "mylessons:" + userId;
        List<Lesson> mylessons = (List<Lesson>)redisTemplate.opsForValue().get(redisKey);
        //缓存命中
        if(mylessons != null) {
            return Result.success(mylessons);
        }
        //缓存未命中，从数据库查询
        mylessons = lessonService.getLessonsByUserId(userId);

        // 设置过期时间为一个小时
        // 键值对形如  mylessons:1 ==> [Lesson1, Lesson2, ...]
        redisTemplate.opsForValue().set(redisKey,
                mylessons,
                1,
                java.util.concurrent.TimeUnit.HOURS);
        return Result.success(mylessons);
    }

    @TrackTime
    @GetMapping("/lessons")
    public Result<Lesson> getLessonById(@RequestParam int id) {
        try{
            Lesson lesson = lessonService.getLessonById(id);
            return Result.success(lesson);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建课程 - 演示事务
     */
    @PostMapping("/createLessons")
    public Result<String> createLesson(@RequestBody Lesson lesson) {
        try {
            String result = lessonService.createLesson(lesson);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量创建课程 - 演示事务传播和回滚
     */
    @SaCheckPermission("CREATE")
    @PostMapping("/lessons/batch")
    public Result<String> batchCreateLessons(@RequestBody List<Lesson> lessons) {
        try {
            lessonService.batchCreateLessons(lessons);
            return Result.success("批量创建课程成功");
        } catch (RuntimeException e) {
            return Result.error("批量创建课程失败: " + e.getMessage());
        }
    }

    /**
     * 更新课程 - 演示事务隔离
     */
    @PutMapping("/lessons/{lessonId}")
    public Result<String> updateLesson(@PathVariable int lessonId, @RequestParam String newTitle) {
        try {
            String result = lessonService.updateLesson(lessonId, newTitle);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除课程 - 演示事务回滚
     */
    @SaCheckPermission("DELETE")
    @DeleteMapping("/lessons/{lessonId}")
    public Result<String> deleteLesson(@PathVariable int lessonId) {
        try {
            String result = lessonService.deleteLesson(lessonId);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 复杂课程操作 - 演示嵌套事务
     */
    @PostMapping("/lessons/complex-operation")
    public Result<String> complexLessonOperation() {
        try {
            lessonService.complexLessonOperation();
            return Result.success("复杂课程操作成功");
        } catch (RuntimeException e) {
            return Result.error("复杂课程操作失败: " + e.getMessage());
        }
    }

    /**
     * 异步批量处理课程
     * 演示线程池在批量数据处理中的应用
     */
    @PostMapping("/lessons/async-batch-process")
    public Result<String> asyncBatchProcessLessons(@RequestBody List<Lesson> lessons) {
        try {
            if (lessons == null || lessons.isEmpty()) {
                return Result.error("课程列表不能为空");
            }

            // 启动异步批量处理
            lessonService.asyncBatchProcessLessons(lessons);

            return Result.success("异步批量处理任务已启动，共 " + lessons.size() + " 门课程");

        } catch (Exception e) {
            return Result.error("启动异步批量处理失败: " + e.getMessage());
        }
    }

    /**
     * 异步生成课程报告
     * 演示线程池在报告生成中的应用
     */
    @PostMapping("/lessons/async-report")
    public Result<String> asyncGenerateReport() {
        try {
            // 启动异步报告生成
            lessonService.asyncGenerateLessonReport();

            return Result.success("课程报告生成任务已启动，请稍后查看结果");

        } catch (Exception e) {
            return Result.error("启动报告生成失败: " + e.getMessage());
        }
    }

    /**
     * 异步同步用户课程数据
     * 演示线程池在数据同步中的应用
     */
    @PostMapping("/lessons/async-sync/{userId}")
    public Result<String> asyncSyncUserLessons(@PathVariable int userId) {
        try {
            // 启动异步数据同步
            lessonService.asyncSyncLessonData(userId);

            return Result.success("用户课程数据同步任务已启动，用户ID: " + userId);

        } catch (Exception e) {
            return Result.error("启动数据同步失败: " + e.getMessage());
        }
    }
}