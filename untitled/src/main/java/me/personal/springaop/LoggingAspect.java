package me.personal.springaop;

import com.google.gson.Gson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * =====================================================================
 * SPRING AOP — same annotations, but runs via runtime proxy
 * =====================================================================
 * <p>
 * KEY DIFFERENCES from pure AspectJ:
 * - Must be a Spring @Component (so Spring knows about it)
 * - Only works on Spring beans (not plain "new" objects)
 * - Only supports execution() pointcut (no call, get, set)
 * - No circular precedence issue (Spring handles ordering)
 * - No compile-time weaving plugin needed
 * <p>
 * =====================================================================
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private final Gson gson = new Gson();


    // =================================================================
    //  1. @Before — runs BEFORE the method
    // =================================================================

    @Before("execution(* me.personal.springaop.Service.add(int, int))")
    public void beforeAdd(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("[BEFORE] About to add: {} + {}", args[0], args[1]);
    }


    // =================================================================
    //  2. @After — runs AFTER the method (always, like finally)
    // =================================================================

    @After("execution(* me.personal.springaop.Service.processPayment(..))")
    public void afterProcessPayment(JoinPoint joinPoint) {
        log.info("[AFTER] processPayment completed (success or failure)");
    }


    // =================================================================
    //  3. @AfterReturning — runs only on successful return
    // =================================================================

    @AfterReturning(
            pointcut = "execution(String me.personal.springaop.Service.findUser(..))",
            returning = "result"
    )
    public void afterFindUser(JoinPoint joinPoint, String result) {
        log.info("[AFTER_RETURNING] findUser returned: {}", result);
    }


    // =================================================================
    //  4. @AfterThrowing — runs only when method throws
    // =================================================================

    @AfterThrowing(
            pointcut = "execution(* me.personal.springaop.Service.*(..))",
            throwing = "ex"
    )
    public void afterException(JoinPoint joinPoint, Exception ex) {
        log.error("[AFTER_THROWING] {} threw: {}",
                joinPoint.getSignature().toShortString(),
                ex.getMessage());
    }


    // =================================================================
    //  5. @Around with @annotation() — match by custom annotation
    // =================================================================

    @Around("@annotation(me.personal.springaop.LogRequestResponse)")
    public Object aroundLogRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();

        log.info("[AROUND] >>> Request  | {} | args: {}", method, gson.toJson(joinPoint.getArgs()));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        log.info("[AROUND] <<< Response | {} | result: {} | took: {}ms",
                method, gson.toJson(result), duration);

        return result;
    }


    // =================================================================
    //  @annotation() with annotation binding
    // =================================================================

    @AfterReturning(
            pointcut = "@annotation(auditable)",
            returning = "result"
    )
    public void auditMethod(JoinPoint joinPoint, Auditable auditable, Object result) {
        log.info("[AUDIT] action={} | method={} | result={}",
                auditable.action(),
                joinPoint.getSignature().toShortString(),
                result);
    }


    // =================================================================
    //  args() — match by argument types
    // =================================================================

    @Around(value = "execution(* me.personal.springaop.Service.calculateTotal(..)) && args(price, quantity)", argNames = "joinPoint,price,quantity")
    public Object aroundCalculation(ProceedingJoinPoint joinPoint, double price, int quantity) throws Throwable {
        log.info("[AROUND-ARGS] Calculating with price={}, quantity={}", price, quantity);
        Object result = joinPoint.proceed();
        log.info("[AROUND-ARGS] Calculation result: {}", result);
        return result;
    }
}
