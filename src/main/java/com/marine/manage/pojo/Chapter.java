package com.marine.manage.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {
    private String title;
    private String status;
    private int duration;
}
