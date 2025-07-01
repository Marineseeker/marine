package com.marine.manage.mapper;

import com.marine.manage.pojo.Lesson;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LessonMapper {

    List<Lesson> getAllLessons();

    List<Lesson> getLessonsByUserId(int userId);

    void insertLesson(Lesson lesson);

    void updateLessonTitle(@Param("lessonId") int lessonId, @Param("newTitle") String newTitle);

    void deleteLesson(int lessonId);

    void deleteUserLessonRefs(int lessonId);


    Lesson getLessonById(int id);
}
