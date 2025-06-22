package com.marine.manage.Controller;

import cn.dev33.satoken.stp.StpUtil;
import com.marine.manage.annotaion.TrackTime;
import com.marine.manage.mapper.LessonMapper;
import com.marine.manage.pojo.Lesson;
import com.marine.manage.pojo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LessonController {
    private final LessonMapper lessonMapper;

    @GetMapping("/lessons")
    public Result<List<Lesson>> getAllLessons() {
        List<Lesson> lessons = lessonMapper.getAllLessons();
        return Result.success(lessons);
    }

    @TrackTime
    @GetMapping("/mylessons")
    public Result<List<Lesson>> getMyLessons() {
        if (!StpUtil.isLogin()) {
            return Result.error("未登录或token无效！");
        }
        int userId = StpUtil.getLoginIdAsInt();
        List<Lesson> mylessons = lessonMapper.getLessonsByUserId(userId);
        return Result.success(mylessons);
    }
}