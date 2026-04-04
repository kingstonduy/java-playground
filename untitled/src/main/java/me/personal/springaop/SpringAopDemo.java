package me.personal.springaop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringAopDemo {

    private static final Logger log = LoggerFactory.getLogger(SpringAopDemo.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringAopDemo.class, args);

        // Get the Spring-managed (proxied) bean
        Service proxy = context.getBean(Service.class);

        // Plain object — no proxy
        Service plain = new Service();

        log.info("===== Proxy vs Plain object =====");
        log.info("proxy.getClass()  = {}", proxy.getClass());
        log.info("plain.getClass()  = {}", plain.getClass());
        log.info("Is AOP proxy?     = {}", AopUtils.isAopProxy(proxy));
        log.info("Is CGLIB proxy?   = {}", AopUtils.isCglibProxy(proxy));
        log.info("Target class      = {}", AopProxyUtils.ultimateTargetClass(proxy));

        log.info("\n===== Calling proxy.greet() — AOP works =====");
        proxy.greet("duy");

        log.info("\n===== Calling plain.greet() — NO AOP =====");
        plain.greet("duy");

        context.close();
    }
}
