package de.naivetardis.landscaper.annotation;

import de.naivetardis.landscaper.exception.ConnectionException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {
    Class<? extends Exception> exception() default ConnectionException.class;
    int maxAttemps() default 3;

}
