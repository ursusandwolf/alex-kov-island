# Code Review v13 + Подготовка к собеседованию

**Reviewer:** Tech Lead / Staff Engineer  
**Date:** 2026-05-14  
**Версия:** v13 (dev, fully merged)  
**Ключевые изменения:** все issue v12 закрыты, 45 тест-классов, ARCHITECTURE_PRESENTATION.md,
GEMINI.md (AI governance), SimCity roadmap, `@Validated` SimulationProperties

---

## Часть 1: Code Review

### Прогресс — все issue v12 закрыты

| Issue из v12 | Статус |
|---|---|
| Awaitility не в pom | ✅ Добавлен в `island-app/pom.xml` |
| `npm run test` не в CI | ✅ Добавлен третьим шагом в `frontend-build` job |
| `SimulationProperties` — неполный | ✅ `@Validated`, `@Min/@Max/@NotBlank`, `broadcastInterval`, `broadcastRateMs` |
| Redundant `@Bean simulationPlugins` | ✅ Убран |
| Нет теста валидации в контроллере | ✅ `SnapshotHistoryServiceTest` с `@TempDir` |
| `historyDir` в `@Value` вместо Properties | ⚠️ `@Value` в `SnapshotHistoryService` пока остался |

### Тестовое покрытие — рост по всем модулям

| Модуль | Тест-классов | Ключевые тесты |
|---|---|---|
| `island-engine` | **18** | GameLoop, ECS, SoA thread-safety, SystemExecutionGraph |
| `island-nature` | **19** | Ecosystem balance, feeding mechanics, reproduction, statistics |
| `island-simcity` | 3 | Boundary, smoke, core logic |
| `island-app` | 5 | Controller (`@WebMvcTest`), Service integration, SnapshotHistory |

Тест `SnapshotHistoryServiceTest` использует `@TempDir` — правильный паттерн JUnit 5
для тестирования файловых операций: временная директория создаётся и удаляется автоматически,
нет зависимости от `data/snapshots` на диске разработчика. ✅

### GEMINI.md — появился AI governance документ

Файл содержит правила для AI-ассистента (Gemini/Claude) при работе с проектом:
архитектурные ограничения, стандарты документации, требования к тестам.

```markdown
# Из GEMINI.md
Coverage: Engine: 75%+, Nature: 65%+, SimCity: 60%+
Language: Javadoc is English-only. Internal docs in Russian.
ADR: Major architectural changes must be recorded in docs/adr/
```

Это признак зрелой команды: AI как часть рабочего процесса с явными правилами,
а не бесконтрольный генератор кода. Такой подход уменьшает технический долг от AI-кода.

### Единственное оставшееся замечание

```java
// SnapshotHistoryService.java — @Value вместо SimulationProperties
@Value("${sim.history.dir:data/snapshots}")
private String historyDir;
```

Перенести в `SimulationProperties`:
```java
private String historyDir = "data/snapshots";
```

---

## Часть 2: Подготовка к собеседованию

### Что этот проект реально демонстрирует интервьюеру

Прежде чем готовить ответы, нужно понять, **что видит senior-собеседующий**
когда смотрит на этот репозиторий:

```
Монолит → Multi-module Maven → JPMS → Spring Boot → React
+ ECS, SoA, volatile, AtomicLong, StampedLock
+ ArchUnit, JMH, PITest, revapi
+ 45 тест-классов, Vitest, CI/CD
= 12 месяцев роста опытной команды за короткий срок
```

Это не типичный учебный проект. Это сигнал, что разработчик:
- понимает архитектурные trade-off'ы
- знает как растёт сложность системы
- умеет применять паттерны к реальным задачам
- думает о production-качестве

---

## Демонстрация: сценарий «15 минут на собеседовании»

### Фаза 1 (2 мин) — Показать живую работу

```bash
# Запустить перед встречей
mvn package -pl island-app -am -DskipTests -q
java -jar island-app/target/island-app.jar --spring.profiles.active=nature
# Открыть http://localhost:8080 — симуляция работает
```

**Что показать:**
- Сетка мира в браузере, животные двигаются
- Кнопка Pause/Resume — симуляция останавливается и возобновляется
- Кнопка Save Snapshot — файл сохраняется
- Переключение на SimCity: Stop → Start City
- Swagger UI: `http://localhost:8080/swagger-ui.html` (если добавлен SpringDoc)

**Почему это важно:** 5 секунд живой демо стоит 5 минут объяснений.
Интервьюер видит: «это работает, человек умеет деплоить».

### Фаза 2 (5 мин) — Архитектура одной диаграммой

Открыть `docs/ARCHITECTURE_PRESENTATION.md` и объяснить:

```
«Проект состоит из трёх слоёв:

1. island-engine — движок без знания о домене.
   Знает о SimulationWorld, GameLoop, ECS-компонентах.
   Не знает ни о волках, ни о зданиях.

2. island-nature / island-simcity — плагины домена.
   Реализуют SimulationPlugin<T> → движок запускает их одинаково.

3. island-app — Spring Boot оркестратор.
   REST API, WebSocket, Jackson. Знает о плагинах, движок — нет.»
```

**Акцент:** движок можно опубликовать как отдельный JAR.
`NaturePlugin` и `SimCityPlugin` — это всего лишь зависимости.

### Фаза 3 (5 мин) — Deep dive в одно архитектурное решение

Выбрать **одно** из трёх — то, что вы знаете лучше всего:

**Вариант A: «Как мы решили проблему thread-safety»**
```java
// SimulationService — показать код и объяснить
private volatile SimulationContext<?> context; // volatile — JMM happens-before

public void pause() {
    SimulationContext<?> current = this.context; // local variable capture
    if (current != null) current.gameLoop().pause();
}
```

**Вариант B: «Почему ECS лучше полиморфизма здесь»**
```java
// До: instanceof везде
if (entity instanceof Animal a) { processAnimal(a); }
else if (entity instanceof Biomass b) { processBiomass(b); }

// После: система объявляет что ей нужно
public class AnimalHealthSystem extends NatureEntitySystem {
    public List<Class<? extends Component>> requiredComponents() {
        return List.of(HealthComponent.class, AgeComponent.class);
    }
    // только Animals имеют оба компонента → автоматический filter
}
```

**Вариант C: «Как WebSocket интегрируется с игровым циклом»**
```java
// TickBroadcastTask в Phase.POSTPROCESS — НЕ из Spring @Scheduled
// Потому что симуляция живёт в своём потоке
class TickBroadcastTask implements ScheduledTask {
    public Phase phase() { return Phase.POSTPROCESS; }
    public void tick(int tickCount) {
        messaging.convertAndSend("/topic/world-state", snapshot); // thread-safe
    }
}
```

### Фаза 4 (3 мин) — Показать инструментарий

Открыть `pom.xml` и быстро пройтись:

```
«У нас настроены:
- JaCoCo: coverage gate 65%+ по engine, без этого build падает
- ArchUnit: если engine импортирует nature — тест падает, автоматически
- PITest: mutation testing, 65% для engine
- revapi: бинарная совместимость API между версиями
- JMH: измеряем SoA vs HashMap — 3× быстрее на 10K entities»
```

---

## Вопросы на собеседовании — чего ожидать

### Блок 1: Архитектура и паттерны (ВЫСОКАЯ вероятность)

**«Почему JPMS? Что это дало?»**

Правильный ответ: три конкретные причины:
1. Нельзя скомпилироваться с зависимостью от `engine.internal` — JPMS выдаст ошибку
2. Плагин не видит `ParallelDispatcher`, `PhaseScheduler` — они не экспортированы
3. Документирует публичный API явно: `exports com.island.engine.core` = это API

Ловушка: не говорить «это быстрее» — JPMS не ускоряет runtime.

---

**«Зачем ECS если у вас не Unity/игра на 60fps?»**

Правильный ответ: ECS решил не performance-задачу, а архитектурную:
- До ECS: добавление нового типа существа → изменение всех сервисов (`instanceof` везде)
- После ECS: новый тип = новый Component, системы не меняются
- SoA (AtomicLongArray) — это уже performance, отдельное решение поверх ECS

---

**«Как вы гарантируете что плагины не зависят друг от друга?»**

Правильный ответ: три уровня защиты:
1. Maven: `island-nature` и `island-simcity` не имеют cross-dependency в pom.xml
2. JPMS: ни один не экспортирует в другой
3. ArchUnit: `natureAndSimCityShouldNotDependOnEachOther` — тест падает при нарушении

---

**«Что такое SystemExecutionGraph и зачем он?»**

Правильный ответ: это алгоритм автоматического параллелизма систем.
- Каждая система объявляет `readComponents()` и `writeComponents()`
- Граф строит DAG конфликтов: системы без пересечений → один параллельный батч
- Аналог Unity DOTS Job System — но без C# bursting

---

### Блок 2: Многопоточность (ОБЯЗАТЕЛЬНО подготовьте)

**«Почему `context` в SimulationService помечен `volatile`?»**

Правильный ответ: без `volatile` Java Memory Model не гарантирует, что запись
в `start()` (synchronized) будет видна потоку, читающему в `pause()` (без lock).
`volatile` создаёт happens-before гарантию между записью и чтением.

Дополнительно: `SimulationContext<?> current = this.context` — local variable capture
обеспечивает атомарность: если `start()` запустится между проверкой `!= null`
и вызовом `pause()`, `current` уже держит старую ссылку, NPE не будет.

---

**«Как HealthSoAStore потокобезопасен?»**

Правильный ответ: три слоя:
1. `volatile AtomicLongArray currentEnergy` — volatile на ссылку, чтобы `ensureCapacity`
   был виден всем потокам после роста массива
2. `AtomicLongArray` — `addAndGet()` атомарен, нет CAS-retry под высокой нагрузкой
3. `synchronized ensureCapacity()` — рост массива сериализован, один поток меняет ссылку

---

**«Что произойдёт если REST вызов `pause()` придёт пока SimulationEngine инициализирует новый мир?»**

Правильный ответ: `start()` — synchronized, `pause()` — не synchronized, но читает
через volatile local. В худшем случае `pause()` получит старый context (до старта)
или null (если context обнулён в `doStart()`), и просто не сделает ничего.
Новый контекст после `start()` будет видим через volatile.

Потенциальный улучшение: `AtomicReference<SimulationContext<?>>` для полного CAS.

---

**«Как вы тестируете многопоточный код?»**

Правильный ответ: три подхода в проекте:
1. `SoAStoreTest.store_thread_safety()` — 4 потока × 1000 итераций, Awaitility + AtomicInteger
2. `GameLoopConcurrencyTest` — конкурентное добавление задач, проверяем отсутствие ConcurrentModificationException
3. `SimulationServiceIntegrationTest` — Awaitility ждёт статуса `RUNNING` с таймаутом

---

### Блок 3: Spring Boot (ВОПРОСЫ почти гарантированы)

**«Почему `Jackson2ObjectMapperBuilderCustomizer` а не `@Bean ObjectMapper`?»**

Правильный ответ: `@Bean ObjectMapper` отключает всю Spring Boot autoconfiguration Jackson.
Теряются: модуль для Java Time, настройки сериализации null, registration MVC converters.
Customizer — аддитивный, добавляет только Mixin не трогая остальное.

---

**«Зачем `SimulationStartedEvent`? Почему не передать context напрямую в broadcaster?»**

Правильный ответ: Loose coupling. `SimulationBroadcaster` не зависит от `SimulationService`.
При рестарте симуляции `SimulationService` публикует новое событие → broadcaster
получает новый контекст автоматически. Без event: нужен прямой вызов broadcaster
из service, circular dependency или additional indirection.

---

**«Что произойдёт если SimulationPlugin не зарегистрирован как Spring Bean?»**

Правильный ответ: `SimulationService` принимает `List<NamedSimulationPlugin<?>>`.
Если список пуст — `plugins` Map будет пуста. При вызове `start("nature", ...)` → 
`IllegalArgumentException("Unknown plugin: nature")` → `GlobalExceptionHandler` вернёт 400.
В интеграционном тесте это поймает `SimulationServiceIntegrationTest`.

---

**«Как работает WebSocket broadcasting без @Scheduled?»**

Правильный ответ: `TickBroadcastTask` реализует `ScheduledTask` с `Phase.POSTPROCESS`.
Он регистрируется в `GameLoop` через `SimulationBroadcaster.startBroadcasting(event)`.
GameLoop вызывает `tick()` в своём потоке в конце каждого тика.
`SimpMessagingTemplate.convertAndSend()` thread-safe — Spring гарантирует.
Это правильнее чем `@Scheduled`: broadcast синхронизирован с тиком, не живёт сам по себе.

---

### Блок 4: Производительность (для senior-позиций)

**«Что такое SoA и почему быстрее HashMap?»**

Правильный ответ: Structure of Arrays vs Array of Structures.

```
AoS (было):  [Animal{energy:100, age:5}, Animal{energy:80, age:3}, ...]
             ^ в памяти рядом: energy, age, другие поля — cache miss при итерации energy

SoA (стало): energy[] = [100, 80, ...], age[] = [5, 3, ...]
             ^ energy[] в одном cache line — CPU читает 16 значений за одну загрузку
```

JMH показал 3× throughput при sequential read 10K entities.

---

**«Где объектный пул и зачем?»**

Правильный ответ: `CellProcessor` в `ParallelDispatcher` — не создаётся новый Callable
каждый тик. При 10 тиках/сек × 20 чанков = 200 new Callable()/сек без пула.
С пулом: 0 аллокаций в горячем пути, GC под нагрузкой работает реже.

---

**«Big-O вашего game loop»**

Правильный ответ:
```
Один тик = O(W × H × S)
  W × H — число ячеек
  S — число сервисов (фиксировано ~6)
  Внутри ячейки — O(LOD) = ограничено константой (30-500 сущностей)

При параллелизации по чанкам: O(W × H × S / P)
  P — число процессорных ядер
  Линейное масштабирование до P ядер
```

---

**«Как масштабировать до 1M сущностей?»**

Правильный ответ: текущая архитектура не масштабируется туда без изменений.
Нужно:
1. Пространственный индекс (QuadTree / hashing) для поиска соседей — сейчас O(8) neighbors
2. Разбиение мира по нодам JVM (Akka Cluster / gRPC) — граничные клетки обменивают state
3. Полный SoA без объектов: только массивы `int[]` по entityId — zero object overhead
4. SIMD через Panama Vector API (Java 21+) — параллельная арифметика на 256bit регистрах

---

### Блок 5: Frontend (если позиция Full-Stack)

**«Почему Zustand а не Redux?»**

Правильный ответ: Redux требует action → reducer → selector для каждого изменения state.
Для простого reactive state симулятора это 3× больше boilerplate без выгоды.
Zustand: `set({ status: 'PAUSED' })` — прямо, без Redux ceremony.
TypeScript типизация из коробки. Поддержка devtools через middleware.

---

**«Как STOMP работает поверх WebSocket?»**

Правильный ответ: STOMP — текстовый протокол поверх WebSocket:
```
WebSocket — transport (raw bytes)
STOMP — messaging (topic-based pub/sub)
SockJS — fallback (long-polling если WS недоступен)

subscribe('/topic/world-state') → Spring router → broadcast всем подписчикам
```

---

**«Как тестируется компонент WorldCanvas?»**

Правильный ответ: Canvas API в jsdom не рендерит реально.
Тест проверяет: `canvas` элемент существует, `onClick` вызывается с правильными координатами.
`getBoundingClientRect` мокируется (`vi.fn()`) — возвращает фиксированный прямоугольник.
`fireEvent.click` с `clientX=15, clientY=15` → `Math.floor(15/cellSize)` = 1 → `onCellClick('1,1')`.

---

## Как показать уровень компетенции — конкретные приёмы

### 1. Говорить про trade-off, не про «правильно/неправильно»

❌ «Мы используете ECS потому что это лучше»  
✅ «ECS дал нам расширяемость ценой сложности — теперь каждый разработчик должен
   понимать Component-System модель. Для небольшой команды это overhead, но при
   добавлении новых существ мы не трогаем существующие системы»

### 2. Знать что НЕ идеально в вашем проекте

❌ «Проект готов к продакшн»  
✅ «Мы намеренно не добавили Spring Security — это следующий шаг.
   Сейчас любой может вызвать `POST /simulation/start`.
   Для production нужен как минимум API key или JWT.»

### 3. Ссылаться на конкретные числа и решения

❌ «SoA быстрее»  
✅ «JMH показал 3× throughput при sequential read 10K entities.
   Это важно потому что AnimalHealthSystem обходит всех животных каждый тик.
   При 5000 животных и 10 тиках/сек — это 50K итераций/сек.»

### 4. Объяснить эволюцию

Этот проект прошёл 13 итераций. Покажите это:
«Изначально был монолит с instanceof везде.
После code review мы ввели ECS — это потребовало рефакторинга 6 сервисов.
Потом добавили SoA поверх ECS для производительности — они работают вместе.»

Эволюция кода — признак взрослого разработчика.

### 5. Уметь объяснить любой файл без открытия IDE

Собеседующий может спросить: «Что делает `SystemExecutionGraph.buildSchedule()`?»

Правильный ответ без кода:
«Принимает список EntitySystem. Для каждой пары проверяет, пересекаются ли их
read/write компоненты. Если нет — в одном параллельном батче. Если да — sequential.
Возвращает `List<List<ScheduledTask>>` — группы для параллельного выполнения.»

---

## Темы на которые обратить особое внимание

### Топ-3 темы, которые проверяют на 80% Java-собеседований:

**1. Java Memory Model и volatile**

Нужно чётко объяснить:
- Что такое happens-before
- Когда volatile недостаточно (compound actions)
- Разница между volatile и synchronized
- AtomicLong.compareAndSet() — когда нужен CAS

У вас в проекте: `volatile context`, `AtomicLongArray`, `StampedLock` — всё есть.

**2. Паттерны проектирования с реальным применением**

Знать не «что такое Observer», а «вот здесь у меня Observer через EventBus, вот проблема которую он решил»:

| Паттерн | Где в проекте | Зачем |
|---|---|---|
| Strategy | `HuntingStrategy`, `ChunkingStrategy` | Заменяемое поведение без изменения клиента |
| Observer | `EventBus` + `AnimalDiedEvent` | Decoupling: Island не знает о StatisticsService |
| Factory Method | `NamedSimulationPlugin.withConfiguration()` | Синглтон как фабрика с параметрами |
| Template Method | `NatureEntitySystem.doProcessTile()` | Базовый алгоритм, подклассы — детали |
| Plugin/SPI | `SimulationPlugin<T>` | Движок не знает о домене |
| Composite | `SystemExecutionGraph` | Группы задач как расписание |

**3. Spring Boot lifecycle**

Знать порядок:
1. `@PostConstruct` → `@EventListener(ApplicationStartedEvent)` → `@PreDestroy`
2. Почему `ApplicationStartedEvent` а не `@PostConstruct` для старта симуляции
   (симуляции нужны все beans, @PostConstruct не гарантирует это)
3. Как `@WebMvcTest` отличается от `@SpringBootTest`
4. Что делает `@ConfigurationProperties` vs `@Value`

---

## Итоговые оценки v13

| Критерий | v1 | v8 | v12 | v13 |
|---|---|---|---|---|
| **Архитектура движка** | 6.5 | 9.0 | 9.5 | **9.5** |
| **Код** | 7.0 | 8.5 | 9.5 | **9.5** |
| **Spring Integration** | — | — | 9.0 | **9.5** |
| **Frontend** | — | — | 7.5 | **8.0** |
| **Тесты** | 5.0 | 8.0 | 9.0 | **9.5** |
| **Production-ready** | — | — | 9.0 | **9.5** |
| **Общая** | 6.5 | 9.0 | 9.5 | **9.5** |

**Проект готов к демонстрации на собеседовании уровня middle+ / senior.**  
Финальный незакрытый пункт — перенести `historyDir` из `@Value` в `SimulationProperties`.
