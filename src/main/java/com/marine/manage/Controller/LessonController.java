package com.marine.manage.Controller;

import cn.dev33.satoken.stp.StpUtil;
import com.marine.manage.annotaion.TrackTime;
import com.marine.manage.mapper.LessonMapper;
import com.marine.manage.pojo.Lesson;
import com.marine.manage.pojo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LessonController {
    private final LessonMapper lessonMapper;
    private final RedisTemplate<String, Object> redisTemplate;

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
        String redisKey = "mylessons:" + userId;
        List<Lesson> mylessons = (List<Lesson>)redisTemplate.opsForValue().get(redisKey);
        //缓存命中
        if(mylessons != null) {
            return Result.success(mylessons);
        }
        //缓存未命中，从数据库查询
        mylessons = lessonMapper.getLessonsByUserId(userId);

        // 设置过期时间为一个小时
        // 键值对形如  mylessons:1 ==> [Lesson1, Lesson2, ...]
        redisTemplate.opsForValue().set(redisKey,
                mylessons,
                1,
                java.util.concurrent.TimeUnit.HOURS);
        return Result.success(mylessons);
    }
}