package com.marine.manage.mapper;

import com.marine.manage.pojo.Lesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
