# Documento de Análisis: Patrones de Diseño GoF en Spring Framework

---

## 1. Portada

- **Título del Trabajo:** Análisis Arquitectónico de Patrones GoF en el Ecosistema de Spring Framework
- **Estudiante:** David Rincón
- **Curso:** Patrones de Diseño de Software
- **Unidad Temática:** Unidad 1 — Fundamentos de Patrones de Diseño y Buenas Prácticas
- **Nivel Académico:** Sexto Semestre de Ingeniería de Sistemas
- **Fecha:** Septiembre de 2026

---

## 2. Introducción

El diseño de software empresarial contemporáneo demanda arquitecturas capaces de mitigar el acoplamiento rígido, maximizar la extensibilidad y proveer mecanismos no invasivos para la incorporación de requerimientos no funcionales. En este ámbito, Spring Framework y su convención sobre configuración materializada en Spring Boot representan uno de los hitos más paradigmáticos de la ingeniería de software moderna (Walls, 2019). Lejos de reinventar soluciones ad hoc, el núcleo de Spring fundamenta su solidez en la adopción sistemática y rigurosa del catálogo de 23 patrones de diseño propuesto por el *Gang of Four* (Gamma et al., 1994), combinándolos armoniosamente con los principios de diseño orientado a objetos postulados por Robert C. Martin (2018).

El presente documento tiene como objetivo examinar de manera exhaustiva el código fuente real de Spring Framework para identificar, descomponer y contrastar tres patrones GoF correspondientes a categorías ontológicas independientes: Creacional (Singleton), Estructural (Proxy) y de Comportamiento (Observer). A través del estudio de las clases nucleares donde residen estas soluciones, se analiza la problemática específica que resuelven en el ciclo de vida de una aplicación Spring Boot, la evidencia empírica en el código base del framework, el impacto contrafactual de su ausencia y la correlación directa con los principios de diseño SOLID.

---

## 3. Análisis de Patrón 1 (Creacional): Singleton

### 3.1. Identificación y Definición del Patrón
El patrón **Singleton** pertenece a la categoría de **Patrones Creacionales**. De acuerdo con Gamma et al. (1994), el propósito canónico del patrón consiste en asegurar que una clase posea una única instancia a lo largo de la ejecución del sistema y proveer un punto global de acceso a dicha instancia. No obstante, mientras que la formulación tradicional de GoF recurre a un constructor privado y a un método estático global (`getInstance()`) —lo cual introduce un acoplamiento estático que dificulta las pruebas unitarias—, Spring Framework reimplementa conceptualmente este patrón bajo la variante arquitectónica de *Singleton Registry* (Registro de Singletons gestionado por contenedor).

### 3.2. Localización en Spring Framework
- **Clase concreta:** `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry`
- **Módulo:** `spring-beans`
- **Repositorio oficial:** `spring-projects/spring-framework`

### 3.3. Problema Específico que Resuelve en Spring Boot
En una aplicación empresarial desarrollada con Spring Boot, decenas o cientos de componentes de infraestructura y servicios de negocio (anotados con `@Service`, `@Repository` o `@Component`) carecen de estado mutable propio (*stateless*). Instanciar un nuevo objeto cada vez que una clase requiere un colaborador generaría una sobrecarga inadmisible en el recolector de basura (*Garbage Collector*), fragmentación de memoria y tiempos excesivos de inicialización, en particular para componentes pesados como fuentes de datos, administradores de transacciones o clientes HTTP.

`DefaultSingletonBeanRegistry` actúa como el registro centralizado del contenedor de Inversión de Control (IoC), garantizando que, de manera predeterminada (ámbito *singleton*), cada bean definido en el contexto de la aplicación se instancie una única vez por contenedor, manteniéndose en una caché concurrente en memoria y compartiéndose de forma segura entre todos los hilos y componentes que soliciten su inyección (Spring Framework Documentation, 2024).

#### Análisis Contrafactual: ¿Qué ocurriría si Spring Boot no implementara este patrón?
Si Spring Boot careciera del registro de singletons, cada inyección de dependencia requeriría la creación de un nuevo objeto (*prototype* forzado). En un servicio web con alto tráfico (e.g., 5.000 peticiones concurrentes por segundo), se instanciarían millones de objetos transitorios idénticos por minuto para clases puramente lógicas, colapsando el *Heap* de la Máquina Virtual de Java (JVM) e imposibilitando el uso compartido de conexiones pooling y cachés internas. Por otro lado, si los desarrolladores forzaran el patrón GoF clásico con clases estáticas, el código se volvería intesteable, rompiendo la inversión de control.

### 3.4. Evidencia en el Código Fuente de Spring
El siguiente extracto, extraído del archivo [`DefaultSingletonBeanRegistry.java`](evidencia/01-singleton-DefaultSingletonBeanRegistry.java), evidencia cómo el framework administra la tabla de instancias únicas mediante un `ConcurrentHashMap` y sincroniza la creación controlada del singleton a través de una fábrica (`ObjectFactory`):

```java
// Paquete: org.springframework.beans.factory.support
// Clase: DefaultSingletonBeanRegistry.java
public class DefaultSingletonBeanRegistry {

    /** Caché de instancias únicas compartidas: beanName -> beanInstance */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "Bean name must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                try {
                    // Creación perezosa mediante la fábrica inyectada
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                }
                finally {
                    afterSingletonCreation(beanName);
                }
                if (newSingleton) {
                    addSingleton(beanName, singletonObject); // Registro en singletonObjects
                }
            }
            return singletonObject;
        }
    }
}
```

### 3.5. Principios SOLID Reforzados
1. **Principio de Responsabilidad Única (SRP):** Al delegar la garantía de unicidad y el ciclo de vida de la instancia al contenedor (`DefaultSingletonBeanRegistry`), la clase del bean de negocio se mantiene estrictamente concentrada en su lógica de dominio, sin asumir la responsabilidad de coordinar su propia instanciación ni restringir sus constructores (Martin, 2018).
2. **Principio de Inversión de Dependencias (DIP):** Los consumidores no dependen de una llamada estática a una clase concreta (antipatrón `MiServicio.getInstance()`), sino de abstracciones inyectadas por el contenedor. Esto permite sustituir el bean singleton en entornos de prueba por un doble de prueba o una implementación *mock* sin alterar al consumidor.

---

## 4. Análisis de Patrón 2 (Estructural): Proxy

### 4.1. Identificación y Definición del Patrón
El patrón **Proxy** pertenece a la categoría de **Patrones Estructurales**. Según Gamma et al. (1994), su propósito es proporcionar un sustituto o intermediario (*surrogate*) que controle y envuelva el acceso a otro objeto, permitiendo ejecutar acciones adicionales de manera transparente antes o después de delegar la llamada al objeto real subyacente.

### 4.2. Localización en Spring Framework
- **Clase concreta:** `org.springframework.aop.framework.JdkDynamicAopProxy` (implementa `java.lang.reflect.InvocationHandler` y `AopProxy`)
- **Módulo:** `spring-aop`
- **Repositorio oficial:** `spring-projects/spring-framework`

### 4.3. Problema Específico que Resuelve en Spring Boot
En el desarrollo de software corporativo proliferan los llamados *aspectos transversales* (*cross-cutting concerns*): gestión transaccional (`@Transactional`), control de seguridad basado en roles (`@PreAuthorize`), auditoría de accesos y métricas de desempeño. Si el desarrollador debiera invocar explícitamente la apertura, confirmación (*commit*) o reversión (*rollback*) de transacciones dentro de cada método de servicio, el código de negocio quedaría contaminado con código boilerplate de infraestructura.

Spring AOP emplea el patrón Proxy para interceptar dinámicamente las llamadas hacia las clases de servicio. Cuando un cliente invoca un método de un bean anotado, no interactúa directamente con el objeto real, sino con un intermediario sintético (creado en tiempo de ejecución vía JDK Dynamic Proxy o CGLIB). Este proxy intercepta la invocación, aplica la lógica transversal configurada (por ejemplo, iniciar la transacción en la base de datos), delega la ejecución al método de negocio original y, finalmente, captura posibles excepciones para decidir si realiza el *commit* o el *rollback* (Walls, 2019).

#### Análisis Contrafactual: ¿Qué ocurriría si Spring Boot no implementara este patrón?
En ausencia del patrón Proxy, la programación orientada a aspectos resultaría inviable en Java de forma limpia. Cada servicio requeriría un bloque `try/catch/finally` manual acoplado directamente al `PlatformTransactionManager`, repitiendo decenas de líneas idénticas en cada operación de base de datos. Una alteración en la política transaccional demandaría modificar cientos de archivos de código fuente, propiciando omisiones críticas de transacciones y brechas de seguridad.

### 4.4. Evidencia en el Código Fuente de Spring
El siguiente fragmento, obtenido de [`JdkDynamicAopProxy.java`](evidencia/02-proxy-JdkDynamicAopProxy.java), ilustra la implementación del método `invoke`, el cual intercepta la llamada reflectiva, extrae la cadena de interceptores (*advices*) y delega el flujo a través de un `MethodInvocation`:

```java
// Paquete: org.springframework.aop.framework
// Clase: JdkDynamicAopProxy.java
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private final AdvisedSupport advised;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TargetSource targetSource = this.advised.targetSource;
        Object target = targetSource.getTarget();
        Class<?> targetClass = (target != null ? target.getClass() : null);

        // Obtención de la cadena de interceptores (advices) configurados para el método
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

        if (chain.isEmpty()) {
            // Ejecución reflectiva directa sobre el objeto objetivo
            return AopUtils.invokeJoinpointUsingReflection(target, method, args);
        }
        else {
            // Se encapsula la invocación y se transita por los interceptores (AOP)
            MethodInvocation invocation =
                    new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
            return invocation.proceed();
        }
    }
}
```

### 4.5. Principios SOLID Reforzados
1. **Principio de Abierto/Cerrado (OCP):** Es el principio medular que refuerza el patrón Proxy. La clase de servicio de negocio permanece completamente cerrada a modificaciones de código cuando se introducen nuevas políticas de seguridad, métricas o transacciones; simultáneamente, el sistema está abierto a la extensión de comportamiento mediante la adición de nuevos interceptores en la cadena de AOP (Martin, 2018).
2. **Principio de Responsabilidad Única (SRP):** Desacopla la lógica de infraestructura (apertura de transacciones JDBC, autenticación de tokens JWT) de la lógica de dominio (procesar una orden de compra, calcular un descuento). La clase de negocio solo sabe de su modelo, mientras que el proxy encapsula la orquestación técnica.

---

## 5. Análisis de Patrón 3 (Comportamiento): Observer

### 5.1. Identificación y Definición del Patrón
El patrón **Observer** pertenece a la categoría de **Patrones de Comportamiento**. De acuerdo con Gamma et al. (1994), define una dependencia de uno-a-muchos entre objetos, de tal modo que cuando un objeto (el sujeto o publicador) cambia su estado o dispara un suceso, todos los objetos dependientes (los observadores o suscriptores) son notificados y actualizados de manera automática y desacoplada.

### 5.2. Localización en Spring Framework
- **Clases e interfaces nucleares:**
  - Despachador / Sujeto: `org.springframework.context.event.SimpleApplicationEventMulticaster`
  - Abstracción de Publicación: `org.springframework.context.ApplicationEventPublisher`
  - Abstracción de Observador: `org.springframework.context.ApplicationListener`
- **Módulo:** `spring-context`
- **Repositorio oficial:** `spring-projects/spring-framework`

### 5.3. Problema Específico que Resuelve en Spring Boot
En arquitecturas complejas de software, el acoplamiento directo punto a punto entre subsistemas genera grafos de dependencias enmarañados. Por ejemplo, al completar una venta en un sistema de comercio electrónico, diversos subsistemas deben actuar en consecuencia: el módulo de inventario debe descontar stock, el módulo de marketing debe enviar un correo de fidelización y el subsistema de auditoría debe registrar la traza. Si el servicio de pedidos debiera conocer, inyectar e invocar secuencialmente a cada uno de esos colaboradores, se convertiría en un God Object inestable.

Asimismo, el propio ciclo de arranque y apagado de Spring Boot depende críticamente de notificaciones del sistema: los componentes necesitan saber cuándo el contexto está listo (`ApplicationReadyEvent`), cuándo el servidor web embebido inició (`WebServerInitializedEvent`) o cuándo el contexto se cerró (`ContextClosedEvent`).

Spring Framework implementa el patrón Observer mediante el mecanismo de *Application Events*. Los emisores simplemente inyectan la interfaz abstracta `ApplicationEventPublisher` y publican un evento tipado. El despachador `SimpleApplicationEventMulticaster` localiza en el contenedor a todos los beans que implementen `ApplicationListener` parametrizados con ese tipo de evento (o métodos anotados con `@EventListener`) y los invoca síncrona o asíncronamente (Walls, 2019).

#### Análisis Contrafactual: ¿Qué ocurriría si Spring Boot no implementara este patrón?
Sin el patrón Observer, la inicialización coordinada de subsistemas dentro de Spring Boot sería intratable. Cada componente que requiriera reaccionar al inicio de la aplicación tendría que ser encadenado manualmente en un método monolítico de arranque. Cualquier integración entre módulos heterogéneos requeriría dependencias circulares directas, elevando drásticamente el acoplamiento y la fragilidad ante cambios.

### 5.4. Evidencia en el Código Fuente de Spring
El siguiente extracto, correspondiente a [`SimpleApplicationEventMulticaster.java`](evidencia/03-observer-SimpleApplicationEventMulticaster.java), evidencia la mecánica de iteración y notificación sobre los observadores registrados, admitiendo ejecución síncrona o concurrente a través de un `Executor`:

```java
// Paquete: org.springframework.context.event
// Clase: SimpleApplicationEventMulticaster.java
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));
        Executor executor = getTaskExecutor();

        // Itera sobre todos los observadores suscritos a este tipo específico de evento
        for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            if (executor != null && listener.supportsAsyncExecution()) {
                // Notificación concurrente a través de hilo independiente
                executor.execute(() -> invokeListener(listener, event));
            }
            else {
                // Notificación síncrona directa
                invokeListener(listener, event);
            }
        }
    }

    protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        ((ApplicationListener<ApplicationEvent>) listener).onApplicationEvent(event);
    }
}
```

### 5.5. Principios SOLID Reforzados
1. **Principio de Abierto/Cerrado (OCP):** El publicador del evento está completamente cerrado a modificaciones cuando surge la necesidad de agregar un nuevo consumidor. Basta con declarar un nuevo bean que implemente `ApplicationListener<MiEvento>` o use `@EventListener`; Spring Boot lo detectará y suscribirá automáticamente sin alterar una sola línea del servicio emisor (Martin, 2018).
2. **Principio de Inversión de Dependencias (DIP):** El sujeto publicador no depende de las clases receptoras concretas, sino de la abstracción `ApplicationEventPublisher` y del contrato formal del evento emitido. De igual manera, los observadores dependen de la interfaz genérica `ApplicationListener<E>`, desacoplando emisores y receptores a través de contratos abstractos.

---

## 6. Conclusiones

El análisis sistemático del código fuente de Spring Framework revela que los patrones de diseño del *Gang of Four* no son meras construcciones académicas, sino la piedra angular que permite la longevidad, modularidad y elegancia de los frameworks empresariales líderes en la industria. La articulación armónica entre el patrón creacional Singleton (mediante el registro IoC), el patrón estructural Proxy (habilitando aspectos no invasivos) y el patrón de comportamiento Observer (desacoplando subsistemas mediante eventos orientados al dominio) demuestra que la excelencia en el diseño arquitectónico radica en componer patrones complementarios para salvaguardar la cohesión y minimizar el acoplamiento. Como lección medular para la práctica profesional, se evidencia que los principios SOLID encuentran en los patrones GoF su materialización operativa más efectiva: aplicar conscientemente estos patrones previene la degeneración de sistemas hacia *God Objects* monolíticos y garantiza bases de código abiertas a la extensión, fácilmente testeables y preparadas para la evolución continua.

---

## 7. Referencias

- Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley Professional.
- Martin, R. C. (2018). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
- Spring Framework Documentation. (2024). *Core Technologies: The IoC Container and Spring AOP APIs*. Broadcom / VMware Tanzu. https://docs.spring.io/spring-framework/reference/core.html
- Walls, C. (2019). *Spring in Action* (5th ed.). Manning Publications.
