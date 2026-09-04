// ============================================================================
// Evidencia 2: Patrón Estructural — Proxy (JDK Dynamic Proxy en Spring AOP)
// Repositorio: https://github.com/spring-projects/spring-framework
// Módulo: spring-aop
// Paquete: org.springframework.aop.framework
// Archivo: JdkDynamicAopProxy.java
// ============================================================================

package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;

final class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private final AdvisedSupport advised;

    /**
     * Intercepta la invocación de cualquier método en el objeto proxy generado dinámicamente.
     * Actúa como intermediario sustituto del objeto real (target), inyectando aspectos
     * transversales (como @Transactional, seguridad o métricas) antes y después del joinpoint.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TargetSource targetSource = this.advised.targetSource;
        Object target = targetSource.getTarget();
        Class<?> targetClass = (target != null ? target.getClass() : null);

        // Obtiene la cadena de interceptores (advices) aplicables al método
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

        if (chain.isEmpty()) {
            // Si no hay aspectos, invoca directamente el método sobre el objeto destino
            return AopUtils.invokeJoinpointUsingReflection(target, method, args);
        }
        else {
            // Empaqueta la llamada y delega la ejecución a través de la cadena de interceptores
            MethodInvocation invocation =
                    new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
            return invocation.proceed();
        }
    }
}
