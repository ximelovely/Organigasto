# Plan de Reformulación de Organigasto

Se propone reestructurar la aplicación para centrarse en la gestión de presupuestos por categorías ("sobres"), deudas detalladas con tablas de amortización y suscripciones recurrentes, basándose estrictamente en el diagrama ER v3 proporcionado.

## Cambios Propuestos

### Capa de Datos (Base de Datos)

Se actualizará la base de datos Room para reflejar fielmente las entidades del diagrama ER. Esto implica reemplazar las entidades actuales por las nuevas definiciones que soportan cálculos financieros avanzados.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/paris/AndroidStudioProjects/Organigasto1/app/src/main/java/com/app/organigasto/data/local/AppDatabase.kt)
- Se actualizarán las clases de entidad registradas.
- Se añadirán los nuevos DAOs necesarios.

#### [NEW] Entidades Basadas en ER v3
Se crearán o actualizarán los archivos en `com.app.organigasto.data.local.entity` para que coincidan con el diagrama:
- `Usuario` (con salt y passwordHash).
- `Categoria` (nombre y usuarioId).
- `PresupuestoPeriodo` (gestión de montos por mes/semana/quincena).
- `Gasto` (registro de egresos vinculados a categorías).
- `Suscripcion` y `CobroSuscripcion` (pagos recurrentes).
- `Prestamo` y `PagoPrestamo` (gestión de deudas y amortización).

### Capa de Lógica (Cálculos Financieros)

Se integrará el `FinancialCalculator` para manejar la lógica de negocio de manera aislada de la UI y la DB.

#### [NEW] [FinancialCalculator.kt](file:///C:/Users/paris/AndroidStudioProjects/Organigasto1/app/src/main/java/com/app/organigasto/domain/utils/FinancialCalculator.kt)
- Lógica para `BudgetCalculator`: Estado de sobres y resumen general.
- Lógica para `LoanCalculator`: Cálculos de MSI (Meses Sin Intereses) y Créditos con Interés, generando tablas de amortización completas.
- Lógica para `StatisticsCalculator`: Distribución de gastos y comparativas mensuales.

### Interfaz de Usuario (UI)

Se replanteará la navegación y pantallas principales:
1.  **Inicio (Organigasto)**: Vista de "sobres" con el presupuesto mensual, mostrando cuánto queda en cada categoría.
2.  **Deudas y Préstamos**: Lista de préstamos con vista detallada que incluye la tabla de pagos mensuales y el progreso de liquidación.
3.  **Suscripciones**: Calendario o lista de próximos cobros recurrentes.
4.  **Estadísticas**: Gráficas de distribución y comparativas de meses.

## Plan de Verificación

### Pruebas Automatizadas
- Pruebas unitarias para `FinancialCalculator` (JUnit) para asegurar que los cálculos de préstamos y presupuestos son exactos.
- Pruebas de Room para verificar las relaciones entre `Usuario`, `Prestamo` y `PagoPrestamo`.

### Verificación Manual
- Crear un préstamo con interés y verificar que la tabla de amortización generada coincida con los pagos esperados.
- Registrar gastos en una categoría y verificar que el "sobre" se actualice correctamente (porcentaje usado y restante).
