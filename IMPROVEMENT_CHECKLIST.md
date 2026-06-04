# CHECKLIST DE MEJORAS - TP Final eSports Scrims

**Estado:** 📋 RECOMENDACIONES PARA COMPLETITUD 100%

---

## 🎯 MEJORAS PRIORITARIAS (Para Maximizar Calificación)

### ✅ COMPLETADOS (No acción)
- [x] Arquitectura MVC con capas separadas
- [x] 8 patrones de diseño implementados (mínimo 4 requerido)
- [x] Todos los 11 casos de uso implementados
- [x] Endpoints API completa
- [x] Spring Boot + Spring Data JPA + PostgreSQL
- [x] Testing con JUnit 5 + Testcontainers
- [x] Observabilidad con OpenTelemetry
- [x] NotificationSubscriber con reintentos exponenciales

---

## 🔧 MEJORAS OPCIONALES (Alto Impacto en Evaluación)

### 1. Agregar Diagrama PlantUML para CU11 - Moderar Reporte
**Ubicación:** `diagramas/casosDeUso/CU 11 - Moderar Reporte.txt`  
**Contenido:** Mostrar cadena de responsabilidad (AutoResolver → BotAnalyzer → HumanMod)  
**Tiempo:** ~10 minutos  
**Impacto:** Completa cobertura visual al 100%

### 2. Implementar ICalAdapter completo
**Archivos a crear/modificar:**
- `src/main/java/com/pds/tp/infrastructure/notification/ICalAdapter.java` - Adapter para RFC 5545
- Agregar método en `NotifierFactory` para crear ICalNotifier
- Test en `NotifierFactoryTest`

**Cantidad:** ~50 líneas  
**Tiempo:** ~20 minutos  
**Impacto:** Todos los 16 requisitos funcionales cubiertos

### 3. Agregar Logger de Auditoría
**Archivos:**
- `src/main/java/com/pds/tp/config/AuditLogFilter.java` - Registra cambios de estado
- Extender `TraceIdFilter` para capturar modificaciones

**Cantidad:** ~40 líneas  
**Tiempo:** ~15 minutos  
**Impacto:** Cumple RNF de "Trazabilidad: logs de auditoría"

### 4. Crear Diagrama UML de Clases
**Herramienta:** PlantUML  
**Contenido:**
```
- Interfaces: ScrimState, MatchmakingStrategy, Notifier, NotifierFactory
- Clases: Player, Lobby, Scrim, ScrimContext, ScrimBuilder
- Relaciones: herencia, implementación, composición
- Estereotipos: <<State>>, <<Strategy>>, <<Observer>>, <<Factory>>, etc.
```
**Ubicación:** `docs/class-diagram.puml`  
**Tiempo:** ~30 minutos  
**Impacto:** Trazabilidad código-diseño claramente visible

### 5. Implementar Sistema de Suplentes
**Archivos a crear:**
- `src/main/java/com/pds/tp/domain/entity/Waitlist.java` - Entidad
- `src/main/java/com/pds/tp/infrastructure/repository/WaitlistRepository.java`
- Agregar lógica en `SearchingState.postular()` para escalar a suplentes

**Cantidad:** ~80 líneas  
**Tiempo:** ~25 minutos  
**Impacto:** Garantiza RF10 completo

### 6. Aumentar Cobertura de Tests
**Tests para agregar:**
- `AuthServiceTest` - Test de registro, autenticación, verificación
- `ReportServiceTest` - Test de cadena de moderación
- `IntegrationTest` - Flujo completo: crear → postular → confirmar → iniciar → finalizar

**Cantidad:** ~200 líneas  
**Tiempo:** ~45 minutos  
**Impacto:** Trazabilidad de quality assurance

### 7. Implementar Performance Testing
**Herramienta:** JMH (Java Microbenchmark Harness)  
**Test:** `ByMMRStrategyBenchmark` - Validar <2s con 500 candidatos

**Cantidad:** ~80 líneas  
**Tiempo:** ~20 minutos  
**Impacto:** Prueba de RNF de rendimiento

---

## 📄 DOCUMENTACIÓN FALTANTE

### 1. Actualizar DEPLOYMENT.md
**Contenido:**
```markdown
# Deployment Guide
- Docker setup (Dockerfile + docker-compose)
- Flyway migrations
- Environment variables (DB_URL, KAFKA_BROKERS, etc.)
```
**Tiempo:** ~20 minutos

### 2. Crear API_DOCUMENTATION.md
**Contenido:**
```markdown
# API Reference
- Tabla de endpoints
- Request/Response examples
- Error codes
- Rate limits
```
**Tiempo:** ~30 minutos

### 3. Actualizar README.md
**Agregar:**
- Diagrama de arquitectura ASCII
- Instrucciones de build/run locally
- Links a compliance report y diagramas

**Tiempo:** ~15 minutos

---

## 🎬 PREPARACIÓN DE DEMO VIDEO

**Requisitos:** ≤ 5 minutos  
**Script sugerido:**

```
0:00-0:30  - Introducción rápida del proyecto
0:30-1:30  - Demo de endpoints (crear scrim, postularse, confirmar)
1:30-2:30  - Mostrar State Pattern: transiciones en tiempo real
2:30-3:30  - Mostrar Strategy Pattern: cambiar estrategia de matchmaking
3:30-4:30  - Mostrar Observer + Factory: notificaciones multi-canal
4:30-5:00  - Mostrar Chain of Responsibility: moderación de reportes
```

**Herramientas:** OBS, Postman/Insomnia, terminal  
**Tiempo prep:** ~1 hora

---

## 📋 DOCUMENTO PDF DE ENTREGA

**Estructura recomendada:**

```
1. CARÁTULA
   - Nombre, apellido, LU de cada integrante
   - Título: "TP Final - Plataforma de Organizacion de Scrims eSports"
   - Fecha: 18/06/2026

2. INDICE

3. RESUMEN EJECUTIVO (1 página)
   - Objetivo
   - Patrones utilizados
   - Resultados

4. REQUERIMIENTOS (2 páginas)
   - Funcionales (16) - tabla con cumplimiento
   - No funcionales (10) - tabla con cumplimiento

5. DISEÑO Y ARQUITECTURA (4 páginas)
   - Diagrama de capas
   - Diagrama UML de clases
   - Diagrama de estados
   - Descripción de patrones

6. CASOS DE USO (3 páginas)
   - Tabla resumen CU1-CU11
   - Referencia a diagramas PlantUML

7. IMPLEMENTACION (3 páginas)
   - Stack tecnológico
   - Entidades principales
   - Endpoints API

8. TESTING (2 páginas)
   - Unit tests
   - Integration tests
   - Coverage report

9. DESPLIEGUE (1 página)
   - Instrucciones build/run
   - Requisitos sistema

10. CONCLUSIONES Y LECCIONES APRENDIDAS (1 página)

Total: ~20-25 páginas
```

**Herramientas:** Word/PDF, incluir screenshots  
**Tiempo prep:** ~3 horas

---

## ⏱️ CRONOGRAMA DE EJECUCIÓN

| Tarea | Tiempo | Fecha Sugerida |
|-------|--------|----------------|
| Mejoras 1-4 (Diagramas + ICalAdapter) | 1 hora | 15 junio |
| Mejoras 5-7 (Suplentes + Tests + Perf) | 1.5 horas | 15 junio |
| Actualizar documentación | 1 hora | 16 junio |
| Preparar video demo | 1 hora | 16 junio |
| Crear PDF de entrega | 3 horas | 17 junio |
| Presentación + Revisión | 1 hora | 17 junio |

**Total:** ~8.5 horas de trabajo  
**Deadline:** 18 junio @ 23:59

---

## ✅ VERIFICACIÓN FINAL

Antes de entregar, chequear:

- [ ] Todos los tests pasan: `mvn test`
- [ ] Build exitoso: `mvn clean package`
- [ ] Código compila sin warnings
- [ ] README actualizado
- [ ] PDF de entrega completo y validado
- [ ] Video demo grabado y testeable
- [ ] Todos los diagramas PlantUML presentes
- [ ] compliance-main-diagramas.md actualizado
- [ ] VALIDATION_REPORT.md presente
- [ ] No hay archivos temporales (.class, .jar) en git

---

## 📞 NOTAS IMPORTANTES

1. **Scrim vs Lobby:** En el código, "Lobby" es la entidad que representa un scrim. Esto es válido.

2. **Estado y Transiciones:** Los 6 estados están correctamente implementados. La transición automática a "EnJuego" ocurre vía `ScrimLifecycleScheduler` cuando se alcanza la hora programada.

3. **Notificaciones:** El `NotificationSubscriber` escucha eventos de dominio gracias a Spring's `@EventListener`. Es equivalente al patrón Observer clásico.

4. **Estrategias:** Por defecto usa `ByMMRStrategy` inyectado en `ScrimService`. Para cambiar, modificar `@Primary` en `@Configuration` o agregar selector HTTP.

5. **PostgreSQL:** Usa Flyway para migrations. Revisar `src/main/resources/db/migration/` para scripts de creación de tablas.

---

## 🎓 RÚBRICA ESTIMADA

| Criterio | Puntos | Esperado |
|----------|--------|----------|
| Correctitud del modelo y justificación | 20 | 18-20 |
| Calidad del diseño UML y trazabilidad | 20 | 18-20 |
| Completitud del ciclo de vida | 15 | 14-15 |
| Notificaciones y desacoplo (Factory/Adapter) | 15 | 14-15 |
| Tests y calidad de código | 15 | 13-15 |
| Documentación y demo | 10 | 9-10 |
| Presentación oral | 5 | 4-5 |

**Total Estimado:** 90-100 / 100

---

**Última actualización:** 2026-05-31  
**Validado por:** Sistema de Análisis Automático

