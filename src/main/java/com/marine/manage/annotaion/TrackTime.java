package com.marine.manage.annotaion;

import java.lang.annotation.*;

@Target(ElementType.METHOD)  // 注解使用目标
@Retention(RetentionPolicy.RUNTIME) // 注解的生命周期
@Documented // 是否生成到 Javadoc中
public @interface TrackTime {
    String value() default "";
}
