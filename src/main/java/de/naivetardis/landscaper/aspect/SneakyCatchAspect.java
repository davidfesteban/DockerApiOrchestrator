package de.naivetardis.landscaper.aspect;

import de.naivetardis.landscaper.annotation.Retryable;
import de.naivetardis.landscaper.annotation.SneakyCatch;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

@Aspect
@Component
@Slf4j
public class SneakyCatchAspect {
    @Around("@annotation(de.naivetardis.landscaper.annotation.SneakyCatch)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SneakyCatch sneakyCatch = method.getAnnotation(SneakyCatch.class);

        return delegateAction(joinPoint, sneakyCatch);
    }

    private Object delegateAction(ProceedingJoinPoint joinPoint, SneakyCatch sneakyCatch) throws InvocationTargetException, IllegalAccessException {
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            if(sneakyCatch.exception().isInstance(e)) {
                if(sneakyCatch.log()) {
                    log.info("Error throwed: {}", e.getMessage());
                }

                if (sneakyCatch.recoverClass() != null && StringUtils.hasText(sneakyCatch.recoverMethod())) {
                    return ReflectionUtils.findMethod(sneakyCatch.recoverClass(), sneakyCatch.recoverMethod()).invoke(null, null);
                }
            }
        }

        return result;
    }
}
