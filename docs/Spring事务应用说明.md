# Spring事务应用说明

## 概述

本项目展示了Spring事务在海洋管理系统中的完整应用，包括事务的基本使用、高级特性、最佳实践和测试方法。

## 事务应用位置

### 1. Service层（主要位置）

#### UserService (`src/main/java/com/marine/manage/service/UserService.java`)
- **只读事务**：`getAllUsers()`, `login()` - 使用 `@Transactional(readOnly = true)`
- **写事务**：`register()` - 演示事务的原子性
- **批量操作**：`batchUserOperation()` - 演示事务传播
- **嵌套事务**：`createTestUser()` - 演示事务传播级别

#### LessonService (`src/main/java/com/marine/manage/service/LessonService.java`)
- **事务传播**：`createLessonWithValidation()` - 使用 `REQUIRES_NEW`
- **事务隔离**：`updateLesson()` - 演示并发控制
- **事务回滚**：`deleteLesson()` - 演示异常回滚
- **复杂事务**：`complexLessonOperation()` - 演示嵌套事务

### 2. 配置层

#### TransactionConfig (`src/main/java/com/marine/manage/config/TransactionConfig.java`)
- 配置事务管理器
- 设置事务超时时间
- 配置嵌套事务支持
- 启用事务管理

### 3. Controller层

#### UserController & LessonController
- 调用Service层的事务方法
- 异常处理和事务回滚
- 提供REST API端点

### 4. 测试层

#### TransactionTest (`src/test/java/com/marine/manage/service/TransactionTest.java`)
- 测试各种事务场景
- 验证事务回滚
- 测试事务传播

## 事务特性展示

### 1. ACID特性

#### 原子性 (Atomicity)
```java
@Transactional
public String register(RegisterRequest registerRequest) {
    // 如果任何步骤失败，整个注册过程都会回滚
    userMapper.insertUser(user);
    userMapper.updateUsernameById(user.getId(), defaultUsername);
}
```

#### 一致性 (Consistency)
```java
// 验证业务规则，确保数据一致性
if (userMapper.getUserByEmail(email) != null) {
    throw new RuntimeException("用户已存在");
}
```

#### 隔离性 (Isolation)
```java
@Transactional
public String updateLesson(int lessonId, String newTitle) {
    // 检查并发冲突
    boolean titleConflict = lessons.stream()
        .filter(lesson -> !lesson.getId().equals(lessonId))
        .anyMatch(lesson -> lesson.getTitle().equals(newTitle));
}
```

#### 持久性 (Durability)
- 通过Spring的默认配置确保事务提交后数据持久化

### 2. 事务传播

#### REQUIRED（默认）
```java
@Transactional
public void batchUserOperation() {
    // 如果当前没有事务，创建新事务
    createTestUser("test@example.com", "password");
}
```

#### REQUIRES_NEW
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void createLessonWithValidation(Lesson lesson) {
    // 总是创建新事务，即使外层事务回滚，这个方法也会提交
}
```

### 3. 事务隔离级别

- **READ_COMMITTED**（默认）：防止脏读
- **READ_UNCOMMITTED**：允许脏读
- **REPEATABLE_READ**：防止不可重复读
- **SERIALIZABLE**：最高隔离级别

### 4. 只读事务

```java
@Transactional(readOnly = true)
public List<User> getAllUsers() {
    // 只读事务，提高性能
    return userMapper.getAllUsers();
}
```

## API端点

### 用户相关
- `POST /register` - 用户注册（演示事务原子性）
- `POST /login` - 用户登录（只读事务）
- `POST /batch-operation` - 批量操作（演示事务传播）

### 课程相关
- `GET /lessons` - 获取所有课程（只读事务）
- `POST /lessons` - 创建课程（演示事务）
- `POST /lessons/batch` - 批量创建课程（演示事务回滚）
- `PUT /lessons/{lessonId}` - 更新课程（演示事务隔离）
- `DELETE /lessons/{lessonId}` - 删除课程（演示事务回滚）
- `POST /lessons/complex-operation` - 复杂操作（演示嵌套事务）

## 最佳实践

### 1. 事务边界
- 事务应该定义在Service层
- Controller层不应该直接使用事务
- 事务方法应该尽可能小，只包含必要的业务逻辑

### 2. 异常处理
```java
try {
    String result = userService.register(registerRequest);
    return Result.success(result);
} catch (RuntimeException e) {
    return Result.error(e.getMessage());
}
```

### 3. 只读事务
- 查询操作使用 `@Transactional(readOnly = true)`
- 提高性能，减少锁竞争

### 4. 事务传播
- 根据业务需求选择合适的传播级别
- 避免不必要的嵌套事务

### 5. 测试
- 使用 `@Transactional` 注解进行测试
- 测试事务回滚场景
- 验证事务传播行为

## 运行和测试

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 运行测试
```bash
mvn test -Dtest=TransactionTest
```

### 3. API测试
使用Postman或其他API测试工具测试各个端点，观察事务行为。

## 注意事项

1. **事务超时**：默认30秒，可根据需要调整
2. **异常类型**：默认只对RuntimeException和Error回滚
3. **代理机制**：Spring事务基于AOP，只有public方法才能被代理
4. **自调用问题**：同一个类中的方法调用不会触发事务代理

## 总结

本项目完整展示了Spring事务的应用，包括：
- 基本的事务注解使用
- 高级事务特性（传播、隔离、超时）
- 事务的最佳实践
- 完整的测试覆盖
- 实际业务场景的应用

通过这些示例，可以深入理解Spring事务的工作原理和应用方法。 