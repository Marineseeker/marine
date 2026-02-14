package com.marine.manage.service;

import com.marine.manage.mapper.KnowledgeBaseMapper;
import com.marine.manage.pojo.KnowledgeBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

  private final KnowledgeBaseMapper knowledgeBaseMapper;

  /**
   * 根据用户问题搜索相关知识
   */
  public List<KnowledgeBase> searchRelevantKnowledge(String userMessage) {
    log.info("搜索相关知识，用户问题: {}", userMessage);

    // 提取关键词进行搜索
    List<KnowledgeBase> results = knowledgeBaseMapper.searchByKeyword(userMessage);

    // 如果直接搜索没有结果，尝试分词搜索
    if (results.isEmpty()) {
      results = searchByTokens(userMessage);
    }

    log.info("找到 {} 条相关知识", results.size());
    return results;
  }

  /**
   * 分词搜索
   */
  private List<KnowledgeBase> searchByTokens(String message) {
    // 简单的分词逻辑，可以后续集成更复杂的分词器
    String[] tokens = {"选课", "成绩", "考试", "学籍", "密码", "登录", "课程", "教师", "学分"};

    for (String token : tokens) {
      if (message.contains(token)) {
        List<KnowledgeBase> results = knowledgeBaseMapper.searchByKeyword(token);
        if (!results.isEmpty()) {
          return results;
        }
      }
    }
    return List.of();
  }

  /**
   * 构建包含知识库信息的系统提示词
   */
  public String buildSystemPromptWithKnowledge(String userMessage) {
    List<KnowledgeBase> knowledgeList = searchRelevantKnowledge(userMessage);

    if (knowledgeList.isEmpty()) {
      return getDefaultSystemPrompt();
    }

    StringBuilder prompt = new StringBuilder();
    prompt.append(getDefaultSystemPrompt()).append("\n\n");
    prompt.append("以下是相关的教务系统操作流程和知识库信息，请参考这些信息回答用户问题：\n\n");

    for (KnowledgeBase kb : knowledgeList) {
      prompt.append("【").append(kb.getTitle()).append("】\n");
      prompt.append("分类：").append(kb.getCategory()).append("\n");
      prompt.append("内容：").append(kb.getContent()).append("\n");
      if (kb.getKeywords() != null && !kb.getKeywords().isEmpty()) {
        prompt.append("关键词：").append(kb.getKeywords()).append("\n");
      }
      prompt.append("\n");
    }

    prompt.append("请基于以上知识库信息，结合你的理解，为用户提供准确、详细的回答。");

    return prompt.toString();
  }

  private String getDefaultSystemPrompt() {
    return """
            你是一个高校教务系统网站的智能助手。
            你的目标是帮助师生快速解决与教务相关的问题，提供简洁、准确的中文回答。
            你可以协助的范围包括但不限于：
            - 课程信息：查询课程简介、上课时间、授课教师、选课规则。
            - 成绩与考试：说明成绩查询流程、考试安排、补考重修政策。
            - 教学计划：介绍培养方案、学分要求、必修/选修课程。
            - 学籍管理：休学、复学、转专业、毕业审核等流程指引。
            - 系统使用：指导用户如何在教务系统中操作（如选课、成绩单导出、考试报名等）。
            - 常见问题：如忘记密码、账号锁定、报错提示。
            
            回答要求：
            1. 始终使用简洁、专业、礼貌的中文。
            2. 回答以"操作步骤"或"关键提示"为主，不要输出冗长解释。
            3. 遇到涉及隐私或权限的数据（如具体成绩、身份证号），请提示用户到系统对应模块查询，不要直接给出真实数据。
            4. 当问题超出教务范围时，请礼貌说明自己能力有限，并建议联系学校相关部门。
            """;
  }

  /**
   * 添加知识条目
   */
  public boolean addKnowledge(KnowledgeBase knowledgeBase) {
    knowledgeBase.setCreateTime(LocalDateTime.now());
    knowledgeBase.setUpdateTime(LocalDateTime.now());
    if (knowledgeBase.getEnabled() == null) {
      knowledgeBase.setEnabled(true);
    }
    if (knowledgeBase.getPriority() == null) {
      knowledgeBase.setPriority(1);
    }

    return knowledgeBaseMapper.insert(knowledgeBase) > 0;
  }

  /**
   * 更新知识条目
   */
  public boolean updateKnowledge(KnowledgeBase knowledgeBase) {
    knowledgeBase.setUpdateTime(LocalDateTime.now());
    return knowledgeBaseMapper.updateById(knowledgeBase) > 0;
  }

  /**
   * 删除知识条目
   */
  public boolean deleteKnowledge(Long id) {
    return knowledgeBaseMapper.deleteById(id) > 0;
  }

  /**
   * 查询所有启用的知识条目
   */
  public List<KnowledgeBase> getAllEnabled() {
    return knowledgeBaseMapper.selectAllEnabled();
  }

  /**
   * 分页查询知识条目
   */
  public List<KnowledgeBase> getKnowledgeByPage(int page, int size) {
    int offset = (page - 1) * size;
    return knowledgeBaseMapper.selectByPage(offset, size);
  }

  /**
   * 统计总数
   */
  public int getTotalCount() {
    return knowledgeBaseMapper.count();
  }
}
