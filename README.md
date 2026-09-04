# Post-contenido — Unidad 1: Fundamentos de Patrones de Diseño y Buenas Prácticas

## Descripción
Repositorio del post-contenido de la Unidad 1 de Patrones de Diseño de Software — Sexto Semestre. Contiene dos partes: refactorización SOLID de un God Object (`parte-1-refactorizacion-solid/`) y análisis de patrones GoF en Spring Framework (`parte-2-analisis-gof-spring/`).

---

## Parte 1 — Refactorización SOLID

### Análisis de Violaciones SOLID en `OrderProcessor`

| Principio | Método/Sección afectada | Descripción de la violación |
|---|---|---|
| **SRP** (Single Responsibility Principle) | `calculateTotal` + `applyDiscount` + `saveOrder` + `sendEmail` + `printReport` | La clase monolítica `OrderProcessor` concentra cinco responsabilidades de dominios totalmente divergentes: cálculo fiscal y de importes (`calculateTotal`), cálculo de descuentos comerciales (`applyDiscount`), persistencia de datos (`saveOrder`), integración de infraestructura de mensajería (`sendEmail`) y renderizado de reportes (`printReport`). La clase tiene múltiples razones para cambiar: si cambian las leyes tributarias, si cambia la base de datos o si cambia el formato de correo/reporte, se debe modificar la misma clase, vulnerando la cohesión. |
| **OCP** (Open/Closed Principle) | `applyDiscount(double total, String customerType)` | El método implementa bifurcaciones condicionales rígidas (`if`) comparando literales (`"VIP"`, `"REGULAR"`). Si el negocio requiere incorporar nuevos tipos de clientes (e.g. `CORPORATIVO`, `EMPLEADO`), es forzoso modificar el código fuente de la clase existente en lugar de extenderla mediante polimorfismo, exponiendo el algoritmo a errores de regresión. |
| **DIP** (Dependency Inversion Principle) | Toda la clase (dependencias internas sin abstracciones) | La clase depende directamente de detalles e implementaciones concretas instanciadas internamente (como el almacenamiento en memoria `List<String> orders` y llamadas acopladas a la salida estándar `System.out`), en lugar de depender de abstracciones (interfaces). El alto acoplamiento impide sustituir componentes, realizar pruebas unitarias aisladas mediante mocks o inyectar implementaciones alternativas de persistencia o notificación. |

### Clases Refactorizadas
- **Aplicación de SRP:**
  - `TaxCalculator`: Responsabilidad única de calcular impuestos y totales.
  - `OrderRepository`: Responsabilidad única de persistencia y consulta de órdenes.
  - `EmailNotifier`: Responsabilidad única de despacho de notificaciones por correo.
  - `OrderReporter`: Responsabilidad única de presentación e impresión de reportes.
- **Aplicación de OCP:**
  - `DiscountStrategy`: Interfaz funcional abierta a extensión para el cálculo de descuentos.
  - `VipDiscount`: Estrategia con 15% de descuento para clientes VIP.
  - `RegularDiscount`: Estrategia con 5% de descuento para clientes regulares.
  - `NoDiscount`: Estrategia sin descuento que retorna el total íntegro.
- **Aplicación de DIP:**
  - `OrderService`: Módulo de alto nivel que orquesta el procesamiento de la orden dependiendo exclusivamente de abstracciones y componentes inyectados a través de su constructor.

### Compilación y Ejecución
Para compilar y ejecutar la clase `Main` de demostración:
```bash
cd parte-1-refactorizacion-solid
mvn compile
mvn exec:java -Dexec.mainClass="com.patrones.u1.Main"
```
