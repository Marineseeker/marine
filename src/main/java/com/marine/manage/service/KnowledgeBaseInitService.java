package com.marine.manage.service;

import com.marine.manage.mapper.KnowledgeBaseMapper;
import com.marine.manage.pojo.KnowledgeBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 知识库数据初始化服务
 * 系统启动时自动初始化基础知识库数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseInitService implements CommandLineRunner {

  private final KnowledgeBaseMapper knowledgeBaseMapper;

  @Override
  public void run(String... args) throws Exception {
    initializeKnowledgeBase();
  }

  private void initializeKnowledgeBase() {
    try {
      // 检查是否已有数据
      int count = knowledgeBaseMapper.count();
      if (count > 0) {
        log.info("知识库已有数据，跳过初始化");
        return;
      }

      log.info("开始初始化知识库数据...");

      List<KnowledgeBase> knowledgeList = Arrays.asList(
              // 选课相关
              KnowledgeBase.builder()
                      .title("在线选课操作流程")
                      .category("选课管理")
                      .content("选课操作步骤：\n1. 登录教务系统，点击'选课管理'菜单\n2. 选择对应学期，点击'进入选课'\n3. 在课程列表中筛选或搜索需要的课程\n4. 点击课程后的'选课'按钮\n5. 确认课程信息无误后，点击'确定选课'\n6. 选课成功后会显示'选课成功'提示\n注意事项：\n- 选课前请仔细查看课程时间，避免冲突\n- 部分课程有先修课程要求\n- 选课有时间限制，请在规定时间内完成")
                      .keywords("选课,在线选课,课程选择,选课流程")
                      .priority(5)
                      .enabled(true)
                      .creator("system")
                      .createTime(LocalDateTime.now())
                      .updateTime(LocalDateTime.now())
                      .build(),

              KnowledgeBase.builder()
                      .title("选课时间安排说明")
                      .category("选课管理")
                      .content("选课时间安排：\n第一轮选课：每学期第15-16周\n第二轮选课：每学期第17-18周\n补选阶段：新学期开学第1-2周\n\n各轮次说明：\n- 第一轮：所有学生均可参与，按志愿顺序分配\n- 第二轮：针对第一轮未选中的课程进行补选\n- 补选阶段：针对有余量的课程开放补选\n\n选课结果查询：选课结束后1-2个工作日可查询结果")
                      .keywords("选课时间,选课安排,补选,选课结果")
                      .priority(4)
                      .enabled(true)
                      .creator("system")
                      .createTime(LocalDateTime.now())
                      .updateTime(LocalDateTime.now())
                      .build(),

              // 成绩查询相关
              KnowledgeBase.builder()
                      .title("成绩查询操作指南")
                      .category("成绩管理")
                      .content("成绩查询步骤：\n1. 登录教务系统主页\n2. 点击左侧菜单'成绩管理'\n3. 选择'成绩查询'功能\n4. 选择查询学期（默认当前学期）\n5. 点击'查询'按钮查看成绩\n\n成绩说明：\n- 平时成绩：包括作业、测验、出勤等\n- 期末成绩：期末考试或考核成绩\n- 总评成绩：平时成绩和期末成绩按比例计算\n- 绩点计算：90-100分=4.0，80-89分=3.0，70-79分=2.0，60-69分=1.0，60分以下=0")
                      .keywords("成绩查询,成绩管理,绩点,总评成绩")
                      .priority(5)
                      .enabled(true)
                      .creator("system")
                      .createTime(LocalDateTime.now())
                      .updateTime(LocalDateTime.now())
                      .build(),

              KnowledgeBase.builder()
                      .title("成绩单打印下载")
                      .category("成绩管理")
                      .content("成绩单获取方式：\n1. 在线查看：登录系统后在成绩查询页面直接查看\n2. 导出Excel：点击'导出'按钮下载Excel格式成绩单\n3. 打印PDF：点击'打印'按钮生成PDF版本成绩单\n4. 官方成绩单：到教务处现场申请加盖公章的正式成绩单\n\n注意事项：\n- 在线成绩单仅供参考，正式成绩单需加盖教务处公章\n- 毕业生可申请中英文对照成绩单\n- 成绩单打印需要提前预约")
                      .keywords("成绩单,打印成绩单,导出成绩,官方成绩单")
                      .priority(3)
                      .enabled(true)
                      .creator("system")
                      .createTime(LocalDateTime.now())
                      .updateTime(LocalDateTime.now())
                      .build(),

              // 系统使用相关
              KnowledgeBase.builder()
                      .title("教务系统登录说明")
                      .category("系统使用")
                      .content("教务系统登录方式：\n网址：http://jwxt.学校域名.edu.cn\n账号：学生学号\n初始密码：身份证后8位\n\n登录步骤：\n1. 打开教务系统网址\n2. 输入学号和密码\n3. 输入验证码\n4. 点击'登录'按钮\n\n常见登录问题：\n- 密码错误：尝试身份证后8位或联系教务处重置\n- 验证码错误：刷新页面重新获取验证码\n- 账号被锁定：连续输错密码会锁定，24小时后自动解锁\n- 浏览器兼容：推荐使用Chrome、Firefox等现代浏览器")
                      .keywords("教务系统,登录,学号,密码,验证码")
                      .priority(5)
                      .enabled(true)
                      .creator("system")
                      .createTime(LocalDateTime.now())
                      .updateTime(LocalDateTime.now())
                      .build(),

              KnowledgeBase.builder()
                      .title("密码重置操作指南")
                      .category("系统使用")
                      .content("忘记密码处理方法：\n方法一：在线重置\n1. 在登录页面点击'忘记密码'\n2. 输入学号和身份证后6位\n3. 通过手机验证码或邮箱验证\n4. 设置新密码\n\n方法二：现场处理\n1. 携带学生证到教务处\n2. 填写密码重置申请表\n3. 工作人员核实身份后重置密码\n4. 新密码为身份证后8位\n\n安全建议：\n- 定期更换密码\n- 密码长度至少8位，包含数字和字母\n- 不要使用生日等容易猜测的密码")
                      .keywords("密码重置,忘记密码,登录问题,密码修改")
                      .priority(5)
                      .enabled(true)
                      .creator("system")
                      .createTime(LocalDateTime.now())
                      .updateTime(LocalDateTime.now())
                      .build()
      );

      int successCount = 0;
      for (KnowledgeBase kb : knowledgeList) {
        try {
          if (knowledgeBaseMapper.insert(kb) > 0) {
            successCount++;
          }
        } catch (Exception e) {
          log.error("插入知识条目失败: {}", kb.getTitle(), e);
        }
      }

      log.info("知识库初始化完成，成功插入 {}/{} 条记录", successCount, knowledgeList.size());

    } catch (Exception e) {
      log.error("知识库初始化失败", e);
    }
  }
}
