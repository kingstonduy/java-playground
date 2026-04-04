package me.personal.aop;

import com.google.gson.Gson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * =====================================================================
 *  AOP TUTORIAL — All Advice Types & Pointcut Types
 * =====================================================================
 *
 *  5 ADVICE TYPES (when to run):
 *    1. @Before         — runs BEFORE the method
 *    2. @After          — runs AFTER the method (always, like finally)
 *    3. @AfterReturning — runs AFTER the method returns successfully
 *    4. @AfterThrowing  — runs AFTER the method throws an exception
 *    5. @Around         — wraps the method, controls if/when it runs
 *
 *  POINTCUT TYPES (which methods to match):
 *    1. execution()     — match method execution by signature pattern
 *    2. @annotation()   — match methods with a specific annotation
 *    3. within()        — match all methods in a class/package
 *    4. args()          — match methods by argument types
 *    5. Combining       — use &&, ||, ! to combine pointcuts
 *    6. Named pointcut  — reusable @Pointcut methods
 *
 * =====================================================================
 */
@Aspect
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private final Gson gson = new Gson();

    // =================================================================
    //  NAMED POINTCUT — define once, reuse everywhere
    // =================================================================

    @Pointcut("execution(* me.personal.aop.Service.*(..))")
    public void serviceMethods() {}


    // =================================================================
    //  1. @Before — runs BEFORE the method
    //     POINTCUT: execution() with exact param types
    // =================================================================

    @Before("execution(* me.personal.aop.Service.add(int, int))")
    public void beforeAdd(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("[BEFORE] About to add: {} + {}", args[0], args[1]);
    }


    // =================================================================
    //  2. @After — runs AFTER the method (always, like finally block)
    //     POINTCUT: execution() on specific method
    // =================================================================

    @After("execution(* me.personal.aop.Service.processPayment(..))")
    public void afterProcessPayment(JoinPoint joinPoint) {
        log.info("[AFTER] processPayment completed (success or failure)");
    }


    // =================================================================
    //  3. @AfterReturning — runs only on successful return
    //     POINTCUT: execution() with return value binding
    // =================================================================

    @AfterReturning(
            pointcut = "execution(String me.personal.aop.Service.findUser(..))",
            returning = "result"
    )
    public void afterFindUser(JoinPoint joinPoint, String result) {
        log.info("[AFTER_RETURNING] findUser returned: {}", result);
    }


    // =================================================================
    //  4. @AfterThrowing — runs only when method throws
    //     POINTCUT: execution() with exception binding
    // =================================================================

    @AfterThrowing(
            pointcut = "execution(* me.personal.aop.Service.*(..))",
            throwing = "ex"
    )
    public void afterException(JoinPoint joinPoint, Exception ex) {
        log.error("[AFTER_THROWING] {} threw: {}",
                joinPoint.getSignature().toShortString(),
                ex.getMessage());
    }


    // =================================================================
    //  5. @Around — wraps the method
    //     POINTCUT: @annotation() — match by custom annotation
    // =================================================================

    @Around("execution(* *(..)) && @annotation(me.personal.aop.LogRequestResponse)")
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
    //  @annotation() with annotation binding (read attributes)
    // =================================================================

    @AfterReturning(
            pointcut = "execution(* *(..)) && @annotation(auditable)",
            returning = "result"
    )
    public void auditMethod(JoinPoint joinPoint, Auditable auditable, Object result) {
        log.info("[AUDIT] action={} | method={} | result={}",
                auditable.action(),
                joinPoint.getSignature().toShortString(),
                result);
    }


    // =================================================================
    //  args() — match methods by argument types
    // =================================================================

    @Around("execution(* me.personal.aop.Service.calculateTotal(..)) && args(price, quantity)")
    public Object aroundCalculation(ProceedingJoinPoint joinPoint, double price, int quantity) throws Throwable {
        log.info("[AROUND-ARGS] Calculating with price={}, quantity={}", price, quantity);
        Object result = joinPoint.proceed();
        log.info("[AROUND-ARGS] Calculation result: {}", result);
        return result;
    }
}
