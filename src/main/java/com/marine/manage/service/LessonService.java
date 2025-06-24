package com.marine.manage.service;

import com.marine.manage.mapper.LessonMapper;
import com.marine.manage.pojo.Lesson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonMapper lessonMapper;

    /**
     * 获取所有课程 - 只读事务
     */
    public List<Lesson> getAllLessons() {
        log.info("获取所有课程信息");
        return lessonMapper.getAllLessons();
    }

    /**
     * 根据用户ID获取课程 - 只读事务
     */
    public List<Lesson> getLessonsByUserId(int userId) {
        log.info("获取用户课程信息，用户ID: {}", userId);
        return lessonMapper.getLessonsByUserId(userId);
    }

    /**
     * 创建课程 - 演示事务的原子性
     */
    @Transactional
    public String createLesson(Lesson lesson) {
        log.info("开始创建课程: {}", lesson.getTitle());
        
        // 验证课程标题不能为空
        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            throw new RuntimeException("课程标题不能为空");
        }
        
        // 检查课程是否已存在
        List<Lesson> existingLessons = lessonMapper.getAllLessons();
        boolean exists = existingLessons.stream()
                .anyMatch(existing -> existing.getTitle().equals(lesson.getTitle()));
        
        if (exists) {
            throw new RuntimeException("课程已存在: " + lesson.getTitle());
        }
        
        // 插入课程 - 这里需要先扩展LessonMapper接口
        lessonMapper.insertLesson(lesson);
        
        log.info("课程创建成功: {}", lesson.getTitle());
        return "课程创建成功: " + lesson.getTitle();
    }

    /**
     * 批量创建课程 - 演示事务传播和回滚
     */
    @Transactional
    public void batchCreateLessons(List<Lesson> lessons) {
        log.info("开始批量创建课程，数量: {}", lessons.size());
        
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            try {
                // 调用另一个事务方法
                createLessonWithValidation(lesson);
                log.info("第{}个课程创建成功: {}", i + 1, lesson.getTitle());
            } catch (Exception e) {
                log.error("第{}个课程创建失败: {}", i + 1, lesson.getTitle(), e);
                // 抛出异常触发整个事务回滚
                throw new RuntimeException("批量创建课程失败，第" + (i + 1) + "个课程失败", e);
            }
        }
        
        log.info("批量创建课程完成");
    }

    /**
     * 带验证的课程创建 - 演示事务传播
     * 使用REQUIRES_NEW传播级别，即使外层事务回滚，这个方法的事务也会提交
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLessonWithValidation(Lesson lesson) {
        log.info("验证并创建课程: {}", lesson.getTitle());
        
        // 模拟复杂的验证逻辑
        if (lesson.getTitle().contains("invalid")) {
            throw new RuntimeException("课程标题包含无效字符");
        }
        
        // 模拟业务规则：课程标题长度限制
        if (lesson.getTitle().length() > 50) {
            throw new RuntimeException("课程标题过长，不能超过50个字符");
        }
        
        lessonMapper.insertLesson(lesson);
        log.info("课程验证并创建成功: {}", lesson.getTitle());
    }

    /**
     * 更新课程信息 - 演示事务隔离
     */
    @Transactional
    public String updateLesson(int lessonId, String newTitle) {
        log.info("更新课程信息，ID: {}, 新标题: {}", lessonId, newTitle);
        
        // 检查课程是否存在
        List<Lesson> lessons = lessonMapper.getAllLessons();
        Lesson targetLesson = lessons.stream()
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElse(null);
        
        if (targetLesson == null) {
            throw new RuntimeException("课程不存在，ID: " + lessonId);
        }
        
        // 检查新标题是否与其他课程冲突
        boolean titleConflict = lessons.stream()
                .filter(lesson -> !lesson.getId().equals(lessonId))
                .anyMatch(lesson -> lesson.getTitle().equals(newTitle));
        
        if (titleConflict) {
            throw new RuntimeException("课程标题已存在: " + newTitle);
        }
        
        // 更新课程标题 - 需要扩展LessonMapper接口
        lessonMapper.updateLessonTitle(lessonId, newTitle);
        
        log.info("课程更新成功，ID: {}, 新标题: {}", lessonId, newTitle);
        return "课程更新成功: " + newTitle;
    }

    /**
     * 删除课程 - 演示事务回滚
     */
    @Transactional
    public String deleteLesson(int lessonId) {
        log.info("删除课程，ID: {}", lessonId);
        
        // 检查课程是否存在
        List<Lesson> lessons = lessonMapper.getAllLessons();
        Lesson targetLesson = lessons.stream()
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElse(null);
        
        if (targetLesson == null) {
            throw new RuntimeException("课程不存在，ID: " + lessonId);
        }
        
        // 模拟检查课程是否有关联的用户（实际项目中可能需要查询关联表）
        if (targetLesson.getTitle().contains("protected")) {
            throw new RuntimeException("受保护的课程不能删除: " + targetLesson.getTitle());
        }
        
        // 删除课程 - 需要扩展LessonMapper接口
        lessonMapper.deleteLesson(lessonId);
        
        log.info("课程删除成功，ID: {}", lessonId);
        return "课程删除成功: " + targetLesson.getTitle();
    }

    /**
     * 复杂业务操作 - 演示嵌套事务
     */
    @Transactional
    public void complexLessonOperation() {
        log.info("开始复杂课程操作");
        
        // 创建课程
        Lesson lesson1 = new Lesson();
        lesson1.setTitle("海洋生物学基础");
        createLesson(lesson1);
        
        // 创建另一个课程
        Lesson lesson2 = new Lesson();
        lesson2.setTitle("海洋生态学");
        createLesson(lesson2);
        
        // 模拟一些业务逻辑
        try {
            // 这里可能会失败，导致整个事务回滚
            performAdditionalOperations();
        } catch (Exception e) {
            log.error("复杂操作中的额外操作失败", e);
            throw new RuntimeException("复杂操作失败", e);
        }
        
        log.info("复杂课程操作完成");
    }

    /**
     * 额外操作 - 演示事务传播
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void performAdditionalOperations() {
        log.info("执行额外操作");
        
        // 模拟一些可能失败的操作
        if (Math.random() < 0.3) { // 30%的概率失败
            throw new RuntimeException("额外操作随机失败");
        }
        
        log.info("额外操作执行成功");
    }
} 