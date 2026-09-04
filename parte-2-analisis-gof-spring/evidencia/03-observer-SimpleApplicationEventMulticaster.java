// ============================================================================
// Evidencia 3: Patrón de Comportamiento — Observer (Spring Application Events)
// Repositorio: https://github.com/spring-projects/spring-framework
// Módulo: spring-context
// Paquete: org.springframework.context.event
// Archivo: SimpleApplicationEventMulticaster.java
// ============================================================================

package org.springframework.context.event;

import java.util.concurrent.Executor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;

public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

    /**
     * Despacha y propaga un evento emitido a todos los observadores (ApplicationListener)
     * registrados que estén tipados o interesados en dicho tipo de evento.
     * Implementa la notificación 1-a-N desacoplada del patrón Observer de GoF.
     */
    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));
        Executor executor = getTaskExecutor();

        // Recupera la lista dinámica de observadores interesados en este evento
        for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            if (executor != null && listener.supportsAsyncExecution()) {
                // Notificación asíncrona mediante un pool de hilos
                executor.execute(() -> invokeListener(listener, event));
            }
            else {
                // Notificación síncrona directa en el hilo emisor
                invokeListener(listener, event);
            }
        }
    }

    protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        // Ejecuta el método onApplicationEvent del observador
        ((ApplicationListener<ApplicationEvent>) listener).onApplicationEvent(event);
    }
}
