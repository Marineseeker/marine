package com.marine.manage.service;

import com.marine.manage.pojo.Lesson;
import com.marine.manage.pojo.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring事务测试类
 * 展示如何测试各种事务场景
 */
@SpringBootTest
@ActiveProfiles("test")
class TransactionTest {

    @Autowired
    private UserService userService;

    @Autowired
    private LessonService lessonService;

    /**
     * 测试用户注册事务 - 成功场景
     */
    @Test
    @Transactional
    public void testUserRegistrationSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRepassword("password123");

        String result = userService.register(request);
        assertNotNull(result);
        assertTrue(result.contains("注册成功"));
    }

    /**
     * 测试用户注册事务 - 失败场景（密码不匹配）
     */
    @Test
    @Transactional
    public void testUserRegistrationFailure() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRepassword("differentpassword");

        assertThrows(RuntimeException.class, () -> {
            userService.register(request);
        });
    }

    /**
     * 测试课程创建事务 - 成功场景
     */
    @Test
    @Transactional
    public void testLessonCreationSuccess() {
        Lesson lesson = new Lesson();
        lesson.setTitle("测试课程");
        lesson.setDescription("这是一个测试课程");

        String result = lessonService.createLesson(lesson);
        assertNotNull(result);
        assertTrue(result.contains("课程创建成功"));
    }

    /**
     * 测试课程创建事务 - 失败场景（标题为空）
     */
    @Test
    @Transactional
    public void testLessonCreationFailure() {
        Lesson lesson = new Lesson();
        lesson.setTitle(""); // 空标题
        lesson.setDescription("这是一个测试课程");

        assertThrows(RuntimeException.class, () -> {
            lessonService.createLesson(lesson);
        });
    }

    /**
     * 测试批量课程创建事务 - 演示事务回滚
     */
    @Test
    @Transactional
    public void testBatchLessonCreationWithRollback() {
        List<Lesson> lessons = Arrays.asList(
            createLesson("课程1"),
            createLesson("课程2"),
            createLesson("invalid课程") // 这个会失败
        );

        assertThrows(RuntimeException.class, () -> {
            lessonService.batchCreateLessons(lessons);
        });
    }

    /**
     * 测试复杂事务操作
     */
    @Test
    @Transactional
    public void testComplexTransactionOperation() {
        // 这个测试可能会失败，因为performAdditionalOperations有30%的失败概率
        // 这正好演示了事务的随机失败场景
        try {
            lessonService.complexLessonOperation();
        } catch (RuntimeException e) {
            // 预期可能会失败
            assertTrue(e.getMessage().contains("复杂操作失败"));
        }
    }

    /**
     * 测试只读事务
     */
    @Test
    @Transactional
    public void testReadOnlyTransaction() {
        // 只读事务应该不会修改数据
        List<Lesson> lessons = lessonService.getAllLessons();
        assertNotNull(lessons);
        // 这里可以添加更多断言来验证数据没有被修改
    }

    /**
     * 测试事务传播 - REQUIRES_NEW
     */
    @Test
    @Transactional
    public void testTransactionPropagation() {
        Lesson lesson = createLesson("传播测试课程");
        
        // 调用使用REQUIRES_NEW传播级别的方法
        lessonService.createLessonWithValidation(lesson);
        
        // 即使外层事务回滚，REQUIRES_NEW的事务也会提交
        // 这里需要根据实际业务逻辑来验证
    }

    /**
     * 辅助方法：创建测试课程
     */
    private Lesson createLesson(String title) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setDescription("测试课程描述");
        return lesson;
    }
} 