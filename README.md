# Post-contenido — Unidad 1: Fundamentos de Patrones de Diseño y Buenas Prácticas

**Patrones de Diseño de Software · Sexto Semestre**  
**Estudiante:** David Rincón  
**Repositorio:** `rincon-post1-u1`

---

## Descripción
Repositorio correspondiente a la entrega integral del post-contenido de la Unidad 1 de la asignatura Patrones de Diseño de Software. Esta actividad consolida las buenas prácticas de ingeniería de software a través de dos dimensiones complementarias:
1. **Parte 1 — Refactorización SOLID de un God Object (`parte-1-refactorizacion-solid/`)**: Reestructuración de una clase Java monolítica con múltiples responsabilidades mediante la aplicación estricta de SRP, OCP y DIP en un proyecto Maven funcional.
2. **Parte 2 — Análisis de Patrones GoF en Spring Framework (`parte-2-analisis-gof-spring/`)**: Investigación directa en el código fuente de Spring Framework para identificar, descomponer y evidenciar patrones Creacionales, Estructurales y de Comportamiento, conectándolos con sus principios SOLID subyacentes.

---

## Parte 1 — Refactorización SOLID

Proyecto Maven ejecutable que refactoriza la clase monolítica `OrderProcessor` (God Object) descomponiéndola en componentes modulares, altamente cohesivos y desacoplados.

### Análisis de Violaciones SOLID en `OrderProcessor`

| Principio | Método/Sección afectada | Descripción de la violación |
|---|---|---|
| **SRP** (Single Responsibility Principle) | `calculateTotal` + `applyDiscount` + `saveOrder` + `sendEmail` + `printReport` | La clase monolítica `OrderProcessor` concentra cinco responsabilidades de dominios totalmente divergentes: cálculo fiscal y de importes (`calculateTotal`), cálculo de descuentos comerciales (`applyDiscount`), persistencia de datos (`saveOrder`), integración de infraestructura de mensajería (`sendEmail`) y renderizado de reportes (`printReport`). La clase tiene múltiples razones para cambiar: si cambian las leyes tributarias, si cambia la base de datos o si cambia el formato de correo/reporte, se debe modificar la misma clase, vulnerando la cohesión arquitectónica. |
| **OCP** (Open/Closed Principle) | `applyDiscount(double total, String customerType)` | El método implementa bifurcaciones condicionales rígidas (`if`) comparando literales (`"VIP"`, `"REGULAR"`). Si el negocio requiere incorporar nuevos tipos de clientes (e.g. `CORPORATIVO`, `EMPLEADO`), es forzoso modificar el código fuente de la clase existente en lugar de extenderla mediante polimorfismo, exponiendo el algoritmo a errores de regresión. |
| **DIP** (Dependency Inversion Principle) | Toda la clase (dependencias internas sin abstracciones) | La clase depende directamente de detalles e implementaciones concretas instanciadas internamente (como el almacenamiento en memoria `List<String> orders` y llamadas acopladas a la salida estándar `System.out`), en lugar de depender de abstracciones (interfaces). El alto acoplamiento impide sustituir componentes, realizar pruebas unitarias aisladas mediante mocks o inyectar implementaciones alternativas de persistencia o notificación. |

### Arquitectura Refactorizada
- **Aplicación de SRP (Separación de Responsabilidades):**
  - [`TaxCalculator`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/TaxCalculator.java): Responsabilidad exclusiva del cálculo aritmético e impositivo.
  - [`OrderRepository`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/OrderRepository.java): Responsabilidad exclusiva de persistencia y consulta de órdenes.
  - [`EmailNotifier`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/EmailNotifier.java): Responsabilidad exclusiva de comunicación por correo electrónico.
  - [`OrderReporter`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/OrderReporter.java): Responsabilidad exclusiva de formato y visualización de reportes.
- **Aplicación de OCP (Patrón Strategy):**
  - [`DiscountStrategy`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/DiscountStrategy.java): Interfaz que desacopla los algoritmos de descuento.
  - Implementaciones concretas: [`VipDiscount`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/VipDiscount.java) (15%), [`RegularDiscount`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/RegularDiscount.java) (5%) y [`NoDiscount`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/NoDiscount.java) (0%). Permite agregar nuevos tipos de clientes sin alterar código existente.
- **Aplicación de DIP (Inversión de Dependencias):**
  - [`OrderService`](parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/OrderService.java): Orquestador de alto nivel cuyas dependencias son inyectadas a través del constructor público, dependiendo de contratos desacoplados.

### Compilación y Ejecución
Desde la raíz del repositorio, ejecutar los siguientes comandos:
```bash
cd parte-1-refactorizacion-solid
mvn compile
mvn exec:java -Dexec.mainClass="com.patrones.u1.Main"
```

#### Salida verificable en consola:
```text
[DB] Orden guardada: ORD-001
[EMAIL] Enviando a vip@mail.com confirmación de orden ORD-001
[DB] Orden guardada: ORD-002
[EMAIL] Enviando a reg@mail.com confirmación de orden ORD-002
=== Reporte de Órdenes ===
  ORD-001:354.025
  ORD-002:226.1
```

---

## Parte 2 — Análisis de Patrones GoF en Spring Framework

Investigación rigurosa del código fuente oficial de Spring Framework ([`spring-projects/spring-framework`](https://github.com/spring-projects/spring-framework)).

### Tabla Resumen de Patrones GoF Analizados

| # | Patrón | Categoría | Clase en Spring | Módulo | Principio SOLID Reforzado |
|:---:|:---:|:---:|:---|:---:|:---:|
| 1 | **Singleton** *(Registry)* | Creacional | `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry` | `spring-beans` | **SRP** (aísla ciclo de vida del bean) y **DIP** (inversión de dependencias sin acoplamiento estático) |
| 2 | **Proxy** *(JDK Dynamic)* | Estructural | `org.springframework.aop.framework.JdkDynamicAopProxy` | `spring-aop` | **OCP** (extensión transversal sin alterar servicios) y **SRP** (separa infraestructura de negocio) |
| 3 | **Observer** *(Multicaster)* | Comportamiento | `org.springframework.context.event.SimpleApplicationEventMulticaster` | `spring-context` | **OCP** (adición de listeners desacoplados) y **DIP** (dependencia de contratos de eventos) |

El documento de análisis detallado con las 7 secciones formales, análisis contrafactual, evidencia de código y citas en formato APA se encuentra en:  
👉 [`parte-2-analisis-gof-spring/documento-analisis.md`](parte-2-analisis-gof-spring/documento-analisis.md)

Fragmentos de código fuente oficial utilizados como evidencia empírica:  
👉 [`parte-2-analisis-gof-spring/evidencia/`](parte-2-analisis-gof-spring/evidencia/)

---

## Herramientas Utilizadas
- **Java JDK 21 / 17:** Entorno de ejecución y compilación del lenguaje.
- **Apache Maven 3.9:** Gestión del ciclo de vida del proyecto, dependencias y plugins de compilación y ejecución.
- **Visual Studio Code:** Entorno de desarrollo integrado y edición de código.
- **Git & GitHub:** Sistema de control de versiones y repositorio de entrega remota.
- **Código Fuente de Spring Framework:** Base de código analizada para la investigación de patrones GoF reales.

---

## Conclusiones
La ejecución de esta actividad demostró que la descomposición de un God Object mediante principios SOLID (SRP, OCP y DIP) y patrones clásicos como Strategy transforma un código frágil y monolítico en un sistema modular, testeable y preparado para la extensión sin riesgo de regresiones. Paralelamente, la investigación en el código fuente de Spring Framework evidenció que los patrones GoF (Singleton, Proxy y Observer) constituyen el fundamento operativo sobre el cual reposan los frameworks empresariales de gran escala, resolviendo problemas de consumo de recursos, modularización transversal e interoperabilidad desacoplada. En conjunto, ambas partes consolidan la lección de que los patrones de diseño y los principios SOLID no son abstracciones aisladas, sino herramientas metodológicas indispensables para concebir arquitecturas de software robustas, mantenibles y profesionales.
