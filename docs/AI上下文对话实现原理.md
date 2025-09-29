# AI上下文对话实现原理详解

## 📋 概述

AI基于上下文回答的核心是**对话历史管理**。现在你的`/knowledge`接口已经完全支持上下文对话功能。

## 🔄 上下文对话工作流程

### 1. 会话创建与管理
```
用户首次对话 → 创建会话ID → 存储在内存/Redis中
后续对话 → 使用相同会话ID → 维护对话连续性
```

### 2. 消息历史存储
```java
// 每次对话都会存储消息
chatSessionService.addMessage(sessionId, "user", userMessage, "knowledge_query");
chatSessionService.addMessage(sessionId, "assistant", aiResponse, "knowledge_based");
```

### 3. 上下文构建
```java
// 获取历史消息（最近8-10条）
List<Map<String, String>> contextMessages = chatSessionService.buildContextMessages(sessionId, 8);

// 构建完整的对话链
var promptBuilder = chatClient.prompt().system(systemPrompt);
for (Map<String, String> msg : contextMessages) {
    if ("user".equals(msg.get("role"))) {
        promptBuilder = promptBuilder.user(msg.get("content"));
    } else {
        promptBuilder = promptBuilder.assistant(msg.get("content"));
    }
}
```

## 🎯 知识库+上下文的协同工作

### 原理说明
1. **知识库检索**：根据当前用户问题搜索相关知识
2. **上下文整合**：将历史对话与知识库信息结合
3. **动态提示词**：构建包含知识库+对话历史的超级提示词

### 实际效果演示

**对话场景示例：**

**第一轮对话：**
```
用户：我想选课
AI：（基于知识库）选课操作步骤：
1. 登录教务系统，点击"选课管理"菜单
2. 选择对应学期，点击"进入选课"
...
```

**第二轮对话（有上下文）：**
```
用户：选课时间是什么时候？
AI：（结合上下文+知识库）根据您刚才询问的选课流程，选课时间安排如下：
- 第一轮选课：每学期第15-16周
- 第二轮选课：每学期第17-18周
- 补选阶段：新学期开学第1-2周
```

## 📊 接口使用方式

### 1. 支持上下文的知识库对话
```javascript
// POST /api/chat/knowledge
{
  "message": "选课时间是什么时候？",
  "sessionId": "uuid-session-id", // 可选，不传则自动创建
  "useContext": true  // 是否使用上下文
}

// 响应
{
  "code": 200,
  "data": {
    "response": "AI回复内容...",
    "sessionId": "uuid-session-id",
    "useContext": true
  }
}
```

### 2. 流式上下文对话
```javascript
// POST /api/chat/knowledge-stream
{
  "message": "用户消息",
  "sessionId": "existing-session-id",
  "useContext": true
}

// SSE 事件流
event: session
data: {"sessionId": "uuid"}

event: message  
data: 回复内容片段...

event: complete
data: {"message": "[DONE]", "sessionId": "uuid"}
```

### 3. 会话管理接口
```javascript
// 获取会话列表
GET /api/chat/sessions

// 获取会话消息历史  
GET /api/chat/sessions/{sessionId}/messages

// 删除会话
DELETE /api/chat/sessions/{sessionId}
```

## 🔧 技术实现细节

### 消息存储结构
```java
public class ChatSession {
    private String sessionId;           // 会话ID
    private String userId;             // 用户ID
    private List<ChatMessage> messages; // 消息历史
}

public class ChatMessage {
    private String role;      // "user" 或 "assistant"
    private String content;   // 消息内容
    private String messageType; // "knowledge_based", "text" 等
}
```

### 上下文长度控制
- **单个会话**：最多保存50条消息
- **上下文窗口**：每次对话使用最近8-10条消息
- **会话超时**：30分钟无活动自动清理

### 知识库集成
```java
// 1. 搜索相关知识
List<KnowledgeBase> knowledge = knowledgeBaseService.searchRelevantKnowledge(message);

// 2. 构建系统提示词（包含知识库信息）
String systemPrompt = knowledgeBaseService.buildSystemPromptWithKnowledge(message);

// 3. 添加对话历史
// 4. 生成回答
```

## 💡 使用建议

### 前端实现
1. **会话管理**：维护当前会话ID
2. **参数控制**：提供开关控制是否使用上下文
3. **会话列表**：展示历史会话供用户选择

### 性能优化
1. **会话清理**：定期清理过期会话
2. **消息限制**：控制单会话消息数量
3. **缓存策略**：生产环境使用Redis存储会话

## 🎉 核心优势

1. **智能连贯**：AI能理解对话上下文，回答更连贯
2. **知识精准**：结合学校知识库，回答更准确
3. **灵活控制**：可选择是否使用上下文和知识库
4. **会话管理**：支持多会话并行，历史可查

现在你的教务系统AI助手已经具备了完整的上下文对话能力！🚀
