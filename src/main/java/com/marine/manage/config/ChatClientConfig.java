package com.marine.manage.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

  @Bean
  public ChatClient chatClient(OpenAiChatModel model) {
    return ChatClient.builder(model).defaultSystem("""
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
            """).build();
  }
}
