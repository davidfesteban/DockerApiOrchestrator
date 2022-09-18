package de.naivetardis.landscaper.aspect;

import de.naivetardis.landscaper.annotation.Retryable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class RetryableAspect {

    private int iteration;

    @Around("@annotation(de.naivetardis.landscaper.annotation.Retryable)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        iteration = 0;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Retryable retryableAnnotation = method.getAnnotation(Retryable.class);

        return delegateRecursion(joinPoint, retryableAnnotation);
    }

    private Object delegateRecursion(ProceedingJoinPoint joinPoint, Retryable retryable) {
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            ++iteration;
            if(retryable.exception() == e.getClass() && iteration < retryable.maxAttemps()) {
                result = delegateRecursion(joinPoint, retryable);
            }
        }

        return result;
    }

}
