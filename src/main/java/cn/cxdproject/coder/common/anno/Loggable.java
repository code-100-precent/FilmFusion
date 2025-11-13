package cn.cxdproject.coder.common.anno;



import cn.cxdproject.coder.common.enums.LogType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务日志注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {

    LogType type();

    String value() default "";
} 