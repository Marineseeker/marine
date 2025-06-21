package com.marine.manage.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component // 注入IOC
@Aspect // 标注切面
public class MethodTimingAspect {
    private static final Logger logger = LoggerFactory.getLogger(MethodTimingAspect.class);
    /**
     * 定义切点
     * &#064;Pointcut  注解定义切点
     * 该切点匹配所有使用了 @Log 注解的方法
     */
    @Pointcut("@annotation(com.marine.manage.annotaion.TrackTime)")
    public void timePointCut(){}

    /**
     * 环绕通知
     * &#064;Around 注解定义环绕通知
     * 该方法会在切点方法执行前后执行
     * @param joinPoint 切点连接点
     * @return 返回切点方法的执行结果
     * @throws Throwable 抛出异常
     * 环绕必须返回 Object 类型
     */
    @Around("timePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        logger.info("method {} start", methodName);
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        }catch (Throwable throwable) {
            logger.error("method: {} exception", methodName, throwable);
            throw throwable;
        }

        long endTime = System.currentTimeMillis();
        if(endTime - startTime > 500) {
            logger.warn("method: {} took too long: {} ms", methodName, endTime - startTime);
        } else {
            logger.info("method: {} executed in {} ms", methodName, endTime - startTime);
        }
        return result;
    }
}
