package com.marine.manage.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Lesson {
    private Integer id;
    private String title;
    private String description;
    private Integer createdBy;
    private LocalDateTime createdAt;
}
