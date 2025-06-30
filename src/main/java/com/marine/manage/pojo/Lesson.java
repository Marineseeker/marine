package com.marine.manage.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.marine.manage.utils.TypHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("lessons")
public class Lesson {
    private Integer id;
    private String title;
    private String description;
    
    @TableField("created_by")
    private Integer createdBy;
    
    @TableField("created_at")
    private LocalDateTime createdAt;

    private int difficulty; // 课程难度
    
    @TableField("duration_weeks")
    private int durationWeeks; // 课程持续时间（周）
    
    @TableField("is_free")
    private boolean isFree;
    
    @TableField("enrolled_count")
    private int enrolledCount;

    @TableField(value = "chapters", typeHandler = TypHandler.ListChapter.class)
    private List<Chapter> chapter;

    @TableField(value = "objectives", typeHandler = TypHandler.ListString.class)
    private List<String> objectives; // 课程目标列表
    
    @TableField("image_cover")
    private String imageCover; // 课程封面图片
}
