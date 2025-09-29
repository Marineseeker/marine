package com.marine.manage.mapper;

import com.marine.manage.pojo.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库数据访问层
 */
@Mapper
public interface KnowledgeBaseMapper {

    /**
     * 插入知识条目
     */
    int insert(KnowledgeBase knowledgeBase);

    /**
     * 根据ID更新知识条目
     */
    int updateById(KnowledgeBase knowledgeBase);

    /**
     * 根据ID删除知识条目
     */
    int deleteById(Long id);

    /**
     * 根据ID查询知识条目
     */
    KnowledgeBase selectById(Long id);

    /**
     * 查询所有启用的知识条目
     */
    List<KnowledgeBase> selectAllEnabled();

    /**
     * 根据分类查询知识条目
     */
    List<KnowledgeBase> selectByCategory(@Param("category") String category);

    /**
     * 根据关键词搜索知识条目
     */
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 分页查询知识条目
     */
    List<KnowledgeBase> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计总数
     */
    int count();
}
