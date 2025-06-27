package com.marine.manage.mapper;

import com.marine.manage.pojo.Lesson;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LessonMapper {

    @Select("SELECT * from lessons")
    List<Lesson> getAllLessons();

    @Select("""
        SELECT l.*
        FROM lessons l
        JOIN user_lessons ul ON l.id = ul.lesson_id
        WHERE ul.user_id = #{userId}
        """)
    List<Lesson> getLessonsByUserId(int userId);

    @Insert("INSERT INTO lessons (title, description, created_by, created_at) VALUES (#{title}, #{description}, #{createdBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertLesson(Lesson lesson);

    @Update("UPDATE lessons SET title = #{newTitle} WHERE id = #{lessonId}")
    void updateLessonTitle(@Param("lessonId") int lessonId, @Param("newTitle") String newTitle);

    @Delete("DELETE FROM lessons WHERE id = #{lessonId}")
    void deleteLesson(int lessonId);

    @Delete("DELETE FROM user_lessons WHERE lesson_id = #{lessonId}")
    void deleteUserLessonRefs(int lessonId);
}
