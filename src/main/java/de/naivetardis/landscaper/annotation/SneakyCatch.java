package de.naivetardis.landscaper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SneakyCatch {
    Class<? extends Exception> exception() default Exception.class;

    boolean log() default true;

    Class<?> recoverClass() default Object.class;

    String recoverMethod() default "";
}
