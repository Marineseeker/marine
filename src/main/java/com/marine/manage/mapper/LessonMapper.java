package com.marine.manage.mapper;

import com.marine.manage.pojo.Lesson;
import com.marine.manage.utils.TypHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LessonMapper {

    @Select("SELECT * from lessons")
    @ResultMap("lessonResultMap")
    List<Lesson> getAllLessons();

    @Select("""
        SELECT l.*
        FROM lessons l
        JOIN user_lessons ul ON l.id = ul.lesson_id
        WHERE ul.user_id = #{userId}
        """)
    @ResultMap("lessonResultMap")
    List<Lesson> getLessonsByUserId(int userId);

    @Insert("INSERT INTO lessons (title, description, created_by, created_at, difficulty, duration_weeks, is_free, enrolled_count, objectives, image_cover, chapters) " +
            "VALUES (#{title}, #{description}, #{createdBy}, NOW(), #{difficulty}, #{durationWeeks}, #{isFree}, #{enrolledCount}, " +
            "#{objectives, typeHandler=com.marine.manage.utils.TypHandler$ListString}, #{imageCover}, " +
            "#{chapter, typeHandler=com.marine.manage.utils.TypHandler$ListChapter})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertLesson(Lesson lesson);

    @Update("UPDATE lessons SET title = #{newTitle} WHERE id = #{lessonId}")
    void updateLessonTitle(@Param("lessonId") int lessonId, @Param("newTitle") String newTitle);

    @Delete("DELETE FROM lessons WHERE id = #{lessonId}")
    void deleteLesson(int lessonId);

    @Delete("DELETE FROM user_lessons WHERE lesson_id = #{lessonId}")
    void deleteUserLessonRefs(int lessonId);

    @Select("SELECT * from lessons where id = #{id}")
    @ResultMap("lessonResultMap")
    Lesson getLessonById(int id);
    
    @Results(id = "lessonResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "difficulty", column = "difficulty"),
        @Result(property = "durationWeeks", column = "duration_weeks"),
        @Result(property = "isFree", column = "is_free"),
        @Result(property = "enrolledCount", column = "enrolled_count"),
        @Result(property = "objectives", column = "objectives", typeHandler = TypHandler.ListString.class),
        @Result(property = "imageCover", column = "image_cover"),
        @Result(property = "chapter", column = "chapters", typeHandler = TypHandler.ListChapter.class)
    })
    @Select("SELECT * from lessons")
    List<Lesson> getAllLessonsWithResultMap();
}
