package com.marine.manage.annotaion;

import java.lang.annotation.*;

@Target(ElementType.METHOD)  // 注解适用于方法
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackTime {
    String value() default "";
}
