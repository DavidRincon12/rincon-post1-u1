// ============================================================================
// Evidencia 1: Patrón Creacional — Singleton (Singleton Registry)
// Repositorio: https://github.com/spring-projects/spring-framework
// Módulo: spring-beans
// Paquete: org.springframework.beans.factory.support
// Archivo: DefaultSingletonBeanRegistry.java
// ============================================================================

package org.springframework.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.util.Assert;

public class DefaultSingletonBeanRegistry {

    /** Caché de instancias únicas de beans: bean name -> bean instance */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * Retorna la instancia de bean registrada como singleton para el nombre dado,
     * creándola y registrándola a través de la fábrica proporcionada si aún no existe.
     * Implementa el patrón Singleton Registry gestionado por el contenedor IoC.
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "Bean name must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                // Validación del ciclo de vida y prevención de dependencias circulares
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                try {
                    // Creación perezosa (lazy) controlada de la única instancia
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                }
                finally {
                    afterSingletonCreation(beanName);
                }
                if (newSingleton) {
                    // Registro de la instancia única en la caché concurrente
                    addSingleton(beanName, singletonObject);
                }
            }
            return singletonObject;
        }
    }
}
