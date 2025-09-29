package com.marine.manage.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 知识库实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBase {

    private Long id;

    /**
     * 知识条目标题
     */
    private String title;

    /**
     * 知识分类（课程管理、成绩查询、选课流程、学籍管理等）
     */
    private String category;

    /**
     * 知识内容
     */
    private String content;

    /**
     * 关键词标签，用逗号分隔
     */
    private String keywords;

    /**
     * 优先级（数值越大优先级越高）
     */
    private Integer priority;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private String creator;
}
