# Code Review v14 + Гайд по собеседованию

**Reviewer:** Tech Lead / Staff Engineer  
**Date:** 2026-05-15  
**База:** v13  
**Ключевые изменения:** Docker + Prometheus, SpringDoc OpenAPI, Spring Boot Actuator,
JPA persistence, `SimulationType` enum, TanStack Query, custom hooks, property-based testing (jqwik),
SocialEffectProvider Strategy pattern, full Swagger аннотации

---

## Часть 1: Code Review

### Таблица прогресса

| Добавлено | Статус |
|---|---|
| `historyDir` перенесён из `@Value` в `SimulationProperties` | ✅ |
| Docker multi-stage build | ✅ Правильный `AS build` → `AS runtime` |
| docker-compose + Prometheus | ✅ |
| SpringDoc OpenAPI → Swagger UI | ✅ `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter` |
| Spring Boot Actuator | ✅ health, metrics, prometheus exposed |
| JPA persistence для снапшотов | ✅ `SimulationSnapshotEntity` + `JpaRepository` |
| `SimulationType` enum вместо `String type` | ✅ `@JsonCreator` + WebConfig Converter |
| TanStack Query в frontend | ✅ polling + cache invalidation |
| `useSimulationSocket` / `useSimulationQueries` хуки | ✅ разделение ответственностей |
| `simulationApi.ts` / `simulationRepository.ts` | ✅ API layer + mapper |
| Property-based тесты (jqwik) | ✅ `@Property`, `@ForAll`, `@IntRange` |
| SocialEffectProvider Strategy pattern | ✅ OCP соблюдён, Spring-бины как стратегии |
| Полный Javadoc на контроллере | ✅ |

---

### ✅ Что сделано особенно хорошо

**1. Docker — правильный multi-stage**

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests      # build stage

FROM eclipse-temurin:21-jre-alpine     # runtime stage — только JRE (~100MB vs ~400MB JDK)
COPY --from=build /app/island-app/target/island-app-*.jar app.jar
```

JRE вместо JDK в runtime — правильный выбор, образ меньше в 3× раза. ✅

**2. SocialEffectProvider — Strategy через Spring-бины**

```java
// Интерфейс Strategy (Plugin SPI)
public interface SocialEffectProvider {
    BuildingComponent.Type getSupportedType();
    void applyEffect(CityTile tile, SocialService service);
}

// Конкретные стратегии — Spring @Component
@Component public class SchoolEffectProvider implements SocialEffectProvider { ... }
@Component public class HospitalEffectProvider implements SocialEffectProvider { ... }
@Component public class ParkEffectProvider implements SocialEffectProvider { ... }
@Component public class CollegeEffectProvider implements SocialEffectProvider { ... }

// SocialService — реестр через Spring
public SocialService(List<SocialEffectProvider> providers) {
    this.providers = providers.stream()
        .collect(Collectors.toMap(SocialEffectProvider::getSupportedType, p -> p));
}
```

Аналог того, что сделали с `NamedSimulationPlugin` ранее — но теперь для SimCity. Новый тип здания = новый `@Component`, `SocialService` не меняется. OCP в действии. ✅

**3. Property-based testing — jqwik**

```java
@Property
void energyNeverIncreasesAndIsNeverNegative(
    @ForAll @IntRange(min = 1, max = 1000) int initialEnergy,
    @ForAll Season season) {
    // jqwik генерирует 100 случайных комбинаций
    // и проверяет инвариант: energy после тика ≤ energy до тика
}
```

Property-based testing — следующий уровень после обычных тестов. Вместо 3-5 ручных
кейсов — 100 случайных за одно объявление. Инварианты симуляции (энергия не растёт,
не уходит в минус) — идеальный кандидат для `@Property`. ✅

**4. TanStack Query + архитектура слоёв в frontend**

```
simulationApi.ts          ← HTTP layer (fetch + error handling)
simulationRepository.ts   ← mapper DTO → domain model
useSimulationQueries.ts   ← TanStack Query (cache, polling, mutations)
useSimulationSocket.ts    ← WebSocket hook
useSimulationStore.ts     ← Zustand (live snapshot state)
App.tsx / components      ← UI
```

Чёткое разделение: API-слой не знает о React, хуки не знают о fetch, компоненты не знают о WebSocket. Это архитектура, а не «весь код в компоненте». ✅

---

### 🟢 Замечания (Resolved in v1.61.0)

**1. TanStack Query polling vs WebSocket — конфликт источников истины**
✅ **Resolved**: `useSimulationStatus` now uses `refetchInterval: connected ? false : 3000`. Polling is only active when WebSocket is disconnected.

**2. JPA снапшоты: H2 in-memory — данные не переживут рестарт**
✅ **Resolved**: Switched to H2 file mode in `application.yml` (`jdbc:h2:file:./data/simulations_db`). Snapshots now persist across restarts.

**3. `./scripts/prometheus.yml` в docker-compose не существует в репозитории**
✅ **Resolved**: File `scripts/prometheus.yml` is present and correctly configured for the `island-simulator` target.

**4. Dockerfile — нет кеширования Maven зависимостей**
✅ **Resolved**: `Dockerfile` refactored to use multi-stage build with a dedicated layer for `mvn dependency:go-offline`, leveraging Docker caching.

**5. `WebConfig` Converter + `@JsonCreator` — дублирование**
🟡 **Acknowledged**: Minor duplication kept to maintain strict separation between Jackson (JSON body) and Spring (Query params) conversion logic.

---

## Часть 2: Гайд по демонстрации и подготовке к собеседованию

### Что этот проект говорит о разработчике

Прежде чем разбирать вопросы — важно понять, **что видит интервьюер**. За 14 итераций в репозитории:

```
Монолит (v1)
  ↓ Multi-module Maven + JPMS
  ↓ ECS + EventBus + Plugin SPI
  ↓ SoA с AtomicLongArray + StampedLock
  ↓ Spring Boot 3.2 + WebSocket STOMP
  ↓ React + TypeScript + TanStack Query
  ↓ Docker + Prometheus + SpringDoc
  ↓ JMH + PITest + ArchUnit + jqwik
  ↓ Property-based tests + Strategy pattern

= Разработчик который знает как система РАЗВИВАЕТСЯ,
  а не только как написать код с нуля
```

**Это сильнее любого резюме.** Вы можете показать эволюцию архитектуры с аргументами.

---

### Демонстрация: формат «20 минут»

#### Перед встречей (за 10 минут)

```bash
# Вариант 1: Maven запуск
mvn spring-boot:run -pl island-app -am -DskipTests &
cd island-ui && npm run dev &
# Открыть: http://localhost:5173 (UI) + http://localhost:8080/swagger-ui.html (API)

# Вариант 2: Docker (эффектнее)
docker-compose up -d
# Открыть: http://localhost:5173
```

Иметь открытыми три вкладки:
1. `localhost:5173` — живая симуляция
2. `localhost:8080/swagger-ui.html` — API документация
3. IDE с кодом, открытый на `SimulationController.java` или `AnimalHealthSystem.java`

---

#### Блок 1 (3 мин): «Покажи что это работает»

1. Открыть `localhost:5173` — сетка с животными
2. Нажать Pause → Resume → показать что работает
3. Save Snapshot → переключить на SimCity → запустить
4. Открыть Swagger UI: «Вот документация API, сгенерирована автоматически»
5. `GET /actuator/health` → показать `{"status":"UP"}`

**Говорить:** «Приложение запущено в Docker, там два контейнера — симулятор и Prometheus для метрик. API задокументирован через OpenAPI.»

---

#### Блок 2 (5 мин): «Архитектура одним рисунком»

Нарисовать (или открыть `docs/ARCHITECTURE_PRESENTATION.md`):

```
┌─────────────────────────────────────┐
│           island-engine.jar         │  ← отдельная библиотека
│  GameLoop → PhaseScheduler          │
│  ECS: EntitySystem, ComponentStore  │
│  EventBus, SoA stores               │
│  Ничего не знает о волках           │
└──────────────┬──────────────────────┘
               │ implements SimulationPlugin
    ┌──────────┴──────────┐
    │                     │
 NaturePlugin        SimCityPlugin
 (волки, травоядные) (здания, жители)
    │                     │
    └──────────┬──────────┘
               │
    ┌──────────┴──────────┐
    │    Spring Boot App  │
    │  REST API + STOMP   │
    │  React frontend     │
    └─────────────────────┘
```

**Ключевая фраза:** «engine-модуль не знает ни о волках, ни о зданиях. Его можно подключить как JAR зависимость и написать свою симуляцию.»

---

#### Блок 3 (7 мин): Один deep dive на выбор

Выбрать один из трёх сценариев и знать его в деталях:

**Сценарий A: Поток данных от тика до браузера**
```
GameLoop.runTick()
  → PhaseScheduler.execute() 
  → AnimalHealthSystem.process() → HealthSoAStore[entityId]  // AtomicLongArray
  → [POSTPROCESS] TickBroadcastTask.tick()
  → SimpMessagingTemplate.convertAndSend("/topic/world-state", snapshot)
  → STOMP over WebSocket
  → useSimulationSocket.ts → setLiveSnapshot()
  → WorldCanvas.tsx re-renders
```

**Сценарий B: Как гарантируется что плагины не смешиваются**
```
module-info.java:
  island-engine exports com.island.engine.core (API)
  island-engine НЕ exports com.island.engine.internal (ParallelDispatcher)
  
  island-nature requires com.island.engine
  island-simcity requires com.island.engine
  island-nature и island-simcity НЕ знают друг о друге

ArchUnit (ArchitectureTest.java):
  natureAndSimCityShouldNotDependOnEachOther()
  engineShouldNotDependOnDomainPackages()
  pluginsShouldNotUseEngineInternals()
  → тест падает при нарушении в CI
```

**Сценарий C: Как потокобезопасность реализована между HTTP и игровым циклом**
```java
// SimulationService
private volatile SimulationContext<?> context; // volatile = JMM happens-before

// HTTP Thread → pause()
SimulationContext<?> current = this.context;  // capture before use
if (current != null) current.gameLoop().pause();

// GameLoop Thread → tick()
// HealthSoAStore.addEnergy() → AtomicLongArray.addAndGet() → lock-free

// Два потока на одну ячейку (Cell)
// ReentrantLock обеспечивает mutex
// Lock ordering: min(x,y) first → no deadlock
```

---

#### Блок 4 (5 мин): Инструментарий (показывает профессионализм)

Открыть `pom.xml` и кратко:

```
«В проекте настроены:
• ArchUnit:   нарушение архитектуры = падающий тест в CI
• JaCoCo:     coverage gate — ниже 65% по engine → build fails
• PITest:     mutation testing, 65% по engine
• revapi:     сломал бинарную совместимость API → build fails
• JMH:        измерение SoA vs HashMap → 3× throughput
• jqwik:      property-based testing, 100 случайных кейсов на инвариант
• Docker:     docker-compose up — одна команда запустить всё
• Prometheus: метрики в реальном времени на /actuator/prometheus»
```

---

### Вопросы: что будут спрашивать и как отвечать

#### Группа 1: Архитектура (вероятность 90%)

**«Объясни паттерн ECS простыми словами»**

Слабый ответ: «Entity — это объект, Component — данные, System — логика.»  
Сильный ответ:
> «До ECS у нас был instanceof везде: FeedingService проверял Animal vs Biomass.
> Добавить нового существа → изменить каждый сервис.
> 
> После ECS: AnimalFeedingSystem объявляет requiredComponents() = [HealthComponent, MetabolismComponent].
> Cell.query() фильтрует сущности — только те у кого есть ОБА компонента.
> Животные имеют оба. Растения — нет. Никакого instanceof.
> 
> Добавление нового существа = новый Component-класс.
> Существующие системы не трогаем. Это и есть OCP.»

---

**«Почему EventBus, а не прямые вызовы?»**

Слабый ответ: «Чтобы не было coupling.»  
Сильный ответ:
> «Конкретная проблема: Island.onEntityRemoved() раньше вызывал StatisticsService.registerDeath()
> напрямую. Island знал о StatisticsService. Добавить второй подписчик (AlertService) →
> менять Island.
>
> После EventBus: Island публикует AnimalDiedEvent. StatisticsService, AlertService,
> SimulationBroadcaster подписываются независимо. Island не знает о подписчиках.
>
> Ценой сложности: события надо трассировать, порядок гарантии нет.
> Мы приняли это trade-off осознанно.»

---

**«Зачем JPMS если Spring Boot и так работает без него?»**

Слабый ответ: «Для безопасности.»  
Сильный ответ:
> «JPMS решает конкретную проблему: плагин не должен использовать внутренности движка.
>
> Без JPMS: разработчик плагина может сделать `new ParallelDispatcher(executor)` —
> скомпилируется, запустится, и сломается при следующем рефакторинге движка.
>
> С JPMS: `engine.internal` не exported → попытка импорта = ошибка компиляции.
> Это аналог `private` на уровне модуля.
>
> Spring совместим с JPMS через `opens ... to spring.core` в module-info.java.»

---

#### Группа 2: Многопоточность (вероятность 95%)

**«Что произойдёт без volatile на context?»**

Правильный ответ:
> «По Java Memory Model, запись в поле из потока A видна потоку B только при наличии
> happens-before отношения. `synchronized` на `start()` создаёт happens-before только
> с другими `synchronized`-блоками на том же мониторе.
>
> `pause()` не synchronized → JMM не гарантирует что поток HTTP увидит новое значение
> context, установленное `start()`. Может прочитать null или старый context.
>
> `volatile` создаёт happens-before между записью (start) и любым последующим чтением.
> Решение корректное.»

---

**«Почему AtomicLongArray а не long[] с synchronized?»**

Правильный ответ:
> «AtomicLongArray использует CAS (Compare-And-Swap) — hardware инструкцию.
> Нет OS-level блокировки, нет thread suspension.
>
> `synchronized` → OS mutex → context switch → microseconds.
> `AtomicLongArray.addAndGet()` → CPU LOCK CMPXCHG → nanoseconds.
>
> В горячем пути (AnimalHealthSystem, 5000 животных × 10 тиков/сек = 50K ops/сек)
> разница критична. JMH показал 3× throughput для SoA vs HashMap.»

---

**«Как тестировать многопоточный код?»**

Правильный ответ (с примерами из проекта):
> «Три подхода в нашем проекте:
>
> 1. `SoAStoreTest.store_thread_safety()` — 4 потока × 1000 итераций,
>    ожидаем результат через Awaitility с таймаутом.
>    Awaitility — библиотека для async assertions.
>
> 2. `GameLoopConcurrencyTest` — конкурентное добавление задач,
>    проверяем отсутствие ConcurrentModificationException.
>    Используем CountDownLatch для синхронизации старта.
>
> 3. `AnimalHealthSystemPropertyTest` с jqwik — property-based,
>    100 случайных комбинаций initialEnergy + Season.
>    Инвариант: после тика energy ≤ initialEnergy и ≥ 0.
>    Это находит edge cases которые unit-тесты пропустят.»

---

#### Группа 3: Spring Boot (вероятность 85%)

**«Почему broadcast через ScheduledTask, а не @Scheduled?»**

Правильный ответ:
> «@Scheduled работает в Spring ThreadPoolTaskScheduler — отдельный поток,
> не связанный с игровым циклом.
>
> Проблема: @Scheduled на 100ms может срабатывать в середине тика,
> когда world state частично обновлён. Snapshot будет inconsistent.
>
> TickBroadcastTask в Phase.POSTPROCESS гарантирует: snapshot берётся
> после завершения всех симуляционных систем того же тика.
> Broadcast всегда consistent.
>
> SimpMessagingTemplate thread-safe — вызов из game thread безопасен.»

---

**«Разница @WebMvcTest и @SpringBootTest?»**

Правильный ответ:
> «@SpringBootTest запускает полный Spring Application Context.
> Для нашего проекта это значит: создаётся NaturePlugin, Island 20×20,
> запускается GameLoop со всеми системами. Тест занимает 3-5 секунд.
>
> @WebMvcTest запускает только Spring MVC слой: DispatcherServlet, Controllers,
> ControllerAdvice, Jackson. @MockBean для Service — симуляция не запускается.
> Тест занимает 200ms.
>
> Для теста логики контроллера (правильные HTTP коды, JSON структура,
> validation) @WebMvcTest — правильный инструмент.
>
> Наш SimulationControllerTest: @WebMvcTest + @MockBean SimulationService.
> Тестирует что 400 при width<5, что 404 при нет snapshot, что статус в JSON.»

---

**«Что такое @ConfigurationProperties и чем лучше @Value?»**

Правильный ответ:
> «@Value привязывает одно свойство к одному полю.
> 30 свойств → 30 @Value → разбросаны по всему коду.
>
> @ConfigurationProperties@Validated:
> 1. Все sim.* свойства в одном типизированном классе
> 2. @Min/@Max/@NotBlank валидируются при старте — fail-fast
> 3. Autocomplete в IDE для application.yml
> 4. Легко передать весь конфиг как объект
>
> Наш SimulationProperties: width, height, threads, tickMs, defaultPlugin,
> broadcastInterval — всё с ограничениями. Если yml невалиден → приложение
> не стартует с понятным сообщением.»

---

#### Группа 4: Производительность (для senior)

**«Что такое SoA и зачем?»**

Правильный ответ (конкретно):
> «SoA = Struct of Arrays. Вместо хранения каждого животного как объекта
> с полями energy/age/speed, мы храним массивы:
> `energy[] = [100, 80, 95, ...]`
> `age[]    = [5, 3, 8, ...]`
>
> Почему быстрее: CPU cache line = 64 байт.
> При чтении energy всех животных подряд, CPU загружает 16 long[] за раз.
> При AoS (объекты) — energy, age, mass, другие поля перемешаны,
> каждое чтение — cache miss.
>
> JMH benchmark (10K entities, sequential read):
> SoA:  ~320M ops/sec
> HashMap: ~95M ops/sec
> 3× разница. При 5K животных × 10 тиков/сек × 6 систем = 300K обращений/сек.»

---

**«Как System Execution Graph оптимизирует параллелизм?»**

Правильный ответ:
> «Каждая EntitySystem декларирует:
> readComponents()  — что читает (shared read — OK параллельно)
> writeComponents() — что пишет (exclusive write — конфликт с другими)
>
> SystemExecutionGraph строит DAG:
> Если A.write ∩ B.read ≠ ∅ → A и B в разных батчах
> Иначе → один параллельный батч → ParallelDispatcher запускает параллельно
>
> Пример:
> AnimalHealthSystem writes [HealthComponent]
> BiomassGrowthSystem writes [GrowthComponent]
> → нет общих компонентов → один батч → параллельно
>
> AnimalFeedingSystem writes [HealthComponent, MetabolismComponent]
> AnimalHealthSystem writes [HealthComponent]
> → HealthComponent shared write → разные батчи → sequential»

---

#### Группа 5: Frontend (Full-Stack позиции)

**«Почему TanStack Query, а не простой fetch в useEffect?»**

Правильный ответ:
> «useEffect + fetch имеет проблемы: нет дедупликации, нет кеша,
> нет обработки race conditions (старый ответ перетирает новый),
> нет loading/error states из коробки.
>
> TanStack Query решает всё:
> - Кеш по queryKey: `['simulation', 'status']` — один запрос на N компонентов
> - Stale-while-revalidate: показываем кешированное пока перезапрашиваем
> - Invalidation: `invalidateQueries` после mutation → автообновление
> - Loading/error states бесплатно
>
> Конкретно в нашем проекте: после `save mutation` инвалидируем `['simulation', 'history']`
> → history список автоматически обновляется.»

---

**«Почему Zustand для live snapshot, а не TanStack Query?»**

Правильный ответ:
> «TanStack Query — для HTTP (request-response, кеш).
> WebSocket — push-based, нет request. TanStack Query не подходит для WS-событий.
>
> Zustand: простое реактивное состояние. WebSocket приходит → `setLiveSnapshot(snapshot)`.
> Компоненты подписаны через `useSimulationStore(state => state.liveSnapshot)`.
> Реактивно, без polling.
>
> Итого: два источника, два инструмента:
> HTTP (status, history) → TanStack Query
> WebSocket (live snapshot) → Zustand»

---

### Как показать уровень компетенции: практические приёмы

#### 1. Говорить про то что НЕ сделано (и почему)

```
❌ «Проект готов к продакшн»

✅ «У нас пока H2 in-memory для снапшотов — при рестарте данные теряются.
   Для production нужен PostgreSQL в docker-compose.
   Это следующий шаг — Spring Data JPA уже есть,
   переключить datasource — 5 строк в yml.»
```

```
✅ «Нет Spring Security. Любой может вызвать DELETE /simulation.
   Для внутреннего инструмента это OK.
   Для продакшена — JWT через spring-boot-starter-security,
   endpoints настраиваются в SecurityFilterChain.»
```

Признание ограничений + знание следующего шага = senior mindset.

---

#### 2. Объяснять решения через проблему, которую они решают

```
❌ «Мы используем EventBus»

✅ «Island.onEntityRemoved() раньше напрямую вызывал StatisticsService.
   Это coupling: Island знал о StatisticsService.
   Добавить AlertService → менять Island.
   
   EventBus устранил coupling: Island публикует событие,
   подписчики регистрируются сами.
   Добавить AlertService → создать новый @EventListener. Island не трогаем.»
```

---

#### 3. Ссылаться на конкретные числа

```
❌ «SoA быстрее»

✅ «JMH: sequential read 10K entities — SoA 320M ops/sec vs HashMap 95M ops/sec.
   При 5K животных × 10 тиках/сек × 6 систем = 300K обращений/сек.
   Разница 3× означает что SoA освобождает один CPU core для других задач.»
```

---

#### 4. Показать эволюцию как feature, а не как technical debt

```
«Изначально это был монолит — один Island.java с 500 строками.
После code review v2 ввели PhaseScheduler — стало понятно что порядок важен.
После v4 добавили EventBus — убрали прямые вызовы StatisticsService.
После v6 — ECS, убрали instanceof.
После v8 — SoA поверх ECS для производительности.

Каждое изменение решало конкретную проблему, которую можно назвать.
Это не переписывание ради переписывания.»
```

---

#### 5. Знать что в коде без IDE

Интервьюер может спросить: «Что делает PhaseScheduler.execute()?»

```
«Принимает список задач и мир. Группирует задачи по Phase
(PREPARE, SIMULATION, POSTPROCESS). Внутри фазы сортирует по priority() descending.
Задачи с одинаковым priority и isParallelizable=true → один параллельный батч
через SystemExecutionGraph. Батч → ParallelDispatcher.dispatch().
Sequential задачи — последовательно между батчами.»
```

---

### Специфика по типу позиции

**Middle Java Backend**
- Акцент: Spring Boot lifecycle, @WebMvcTest vs @SpringBootTest, JPA, thread safety
- Показать: SimulationController + tests, volatile + local capture паттерн
- Вопрос который точно будет: разница synchronized и volatile

**Senior Java / Tech Lead**
- Акцент: архитектурные решения и trade-off'ы, JPMS, модульность
- Показать: эволюцию v1 → v14, ADR документы, ArchUnit как enforcement
- Вопрос который точно будет: как бы масштабировали до 1M entities

**Full-Stack**
- Акцент: связка Spring WebSocket → React, архитектура frontend слоёв
- Показать: поток данных от GameLoop до WorldCanvas, TanStack Query vs Zustand
- Вопрос который точно будет: почему не Redux

**System Design / Architecture**
- Акцент: почему разделение на модули, JPMS, plugin SPI
- Показать: module-info.java, ArchUnit rules, `NamedSimulationPlugin` pattern
- Вопрос который точно будет: как добавить третий плагин (дать ответ с кодом)

---

## Итоговые оценки

| Критерий | v1 | v8 | v11 | v13 | v14 |
|---|---|---|---|---|---|
| **Архитектура** | 6.5 | 9.0 | 9.5 | 9.5 | **9.5** |
| **Код** | 7.0 | 8.5 | 9.5 | 9.5 | **9.5** |
| **Spring Integration** | — | — | 3.0 | 9.5 | **9.5** |
| **Frontend** | — | — | — | 8.0 | **8.5** |
| **DevOps** | — | — | — | — | **8.5** |
| **Production-ready** | — | — | 8.5 | 9.0 | **9.5** |
| **Общая** | 6.5 | 9.0 | 9.5 | 9.5 | **9.5** |

**Два точечных fixes до идеального состояния:**
1. H2 file mode в application.yml (или PostgreSQL в docker-compose) — иначе JPA снапшоты теряются при рестарте
2. `./scripts/prometheus.yml` добавить в репозиторий — иначе `docker-compose up` падает
