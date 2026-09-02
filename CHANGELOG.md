# Changelog

## v1.0.0-beta.9

### Архитектура

#### Clean Architecture fix
- `feature:tabs` больше не зависит от `:data` модуля
- Созданы `AIProviderFactory` и `AgentContextRepository` интерфейсы в `domain`
- `AIProviderManager` и `AppDataManager` теперь реализуют domain-интерфейсы

#### HttpClient singleton
- Единый `HttpClient` через Hilt DI (`HttpModule`)
- Убраны дублирующие инстанции из AIProviderManager, LocalAIService, WorkspaceService

### Локализация
- String resources для всех экранов (~200 строк RU+EN)
- Marketplace, Image, Video, Memory, Agents, Templates, Files, Prompts, Plugins, Modules, Chains, ChainDetail, AIRouter, SplitView, VoiceMode, AIProvider, TeamWorkspaces

### Тесты
- ExportViewModelTest (21 тест)
- AgentsViewModelTest (17 тестов)
- MemoryViewModelTest (19 тестов)
- HiltAndroidTest + MainActivityTest smoke test
- LocalAIServiceTest и WorkspaceServiceTest обновлены для HttpClient injection

### Экспорт
- `toJson()` теперь использует `kotlinx.serialization.json` вместо `org.json` (JVM-совместимость)

---

## v1.0.0-beta.8

### Улучшения

#### LazyColumn keys
- Добавлены `key` параметры во все LazyColumn/LazyRow items() вызовы (31 изменение в 22 файлах)
- Улучшена производительность и корректность анимаций

#### Room миграции
- Создан фреймворк миграций (`Migrations.kt`, `ALL_MIGRATIONS` массив)
- `fallbackToDestructiveMigration()` сохранён для беты с TODO перед v1.0.0

#### Единый MessageRole
- Убран дублирующий `MessageRole` enum
- Единый `MessageRole` в `domain.model.MessageRole` (SYSTEM, USER, ASSISTANT)
- Обновлены импорты в 10 файлах

#### Локализация RU+EN
- Созданы `strings.xml` файлы для 5 модулей (core, tabs, settings, localai, app)
- ~40 основных строк вынесены из кода в ресурсы
- Русский и английский варианты

#### Тесты
- Создан `TabRepositoryImplTest` с 10 тестами

---

## v1.0.0-beta.7

### Исправления критических ошибок

#### Багfix
- **Дублирование сообщений** — исправлен баг где userMessage добавлялась 3 раза при отправке
- Сообщения теперь корректно отображаются и сохраняются в БД

#### Безопасность
- API ключи в Marketplace теперь скрыты (PasswordVisualTransformation)
- `allowBackup=false` — предотвращает утечку ключей через ADB backup
- Добавлено разрешение `USE_BIOMETRIC`

#### ProGuard / R8
- Полные правила для Kotlinx Serialization, Room, Ktor
- dontwarn для Android-несовместимых классов Ktor
- Release сборки теперь работают корректно

#### Error Handling
- LocalAIViewModel — try-catch для testConnection, pullModel, deleteModel
- MarketplaceViewModel — try-catch для addProvider
- MemoryViewModel — try-catch для CRUD операций
- AgentsViewModel — try-catch для CRUD операций

---

## v1.0.0-beta.6

### Улучшения и новые возможности

#### Светлая тема
- Полная светлая цветовая схема Material 3
- Toggle "Тёмная тема" в настройках
- Автоматическое определение системной темы

#### Поиск по чатам
- Глобальный поиск по всем вкладкам
- Поиск по названиям и содержимому сообщений
- Результаты с предпросмотром последнего сообщения

#### Улучшенный экспорт
- "Копировать в буфер обмена" для всех форматов
- "Открыть в браузере" для HTML экспорта
- Улучшенный UI с кнопками действий

#### Новые шаблоны задач (16 всего)
- План соцсетей (контент-план на неделю)
- Code Review (анализ кода с рекомендациями)
- Презентация (структура на 10-15 слайдов)
- API Documentation (документация REST API)
- Урок/Курс (обновлённый шаблон)

---

## v1.0.0-beta.5

### Доступность (Accessibility)

#### Масштабирование шрифтов
- 4 размера: Маленький / Стандартный / Большой / Очень большой
- Настройка в "Настройки → Доступность → Размер шрифта"
- Масштабирует все текстовые элементы приложения

#### Высокий контраст
- Увеличенная контрастность текста и элементов
- Белый текст на чёрном фоне вместо приглушённого
- Яркие акцентные цвета для лучшей видимости

#### TalkBack
- Семантические аннотации на вкладках ("Вкладка: название, активная")
- Различение сообщений пользователя и AI в чате
- contentDescription на всех иконках

#### Ошибки
- Экран изображений теперь показывает ошибки (ранее молча проглатывал)
- Экран видео показывает ошибку вместо фейкового URL

---

## v1.0.0-beta.4

### Реальная генерация изображений

#### Image Generation
- **Pollinations.ai** — бесплатная генерация без API ключа (по умолчанию)
- **OpenAI DALL-E 3** — используется если у пользователя есть OpenAI ключ с supportsImages
- Автоматический выбор: если есть DALL-E → используется он, иначе Pollinations
- Картинки загружаются через Coil с кэшированием

#### Video Generation
- Показывается информативное сообщение вместо фейкового URL
- Рекомендация использовать Automation Chains для создания видеоконтента

---

## v1.0.0-beta.3

### Persistence — данные больше не теряются

#### Room DB v3
- **Agents** — хранятся в Room DB, переживают перезапуск приложения
- **Memory entries** — хранятся в Room DB, важные помечены звёздочкой
- AgentDao, MemoryEntryDao с полными CRUD операциями
- AppDataManager перенесён в data module, работает через Room

#### Architecture
- DataModule (Hilt) в data модуле — все Room-зависимости в одном месте
- AIModule в di модуле — только API Key encryption и Provider Manager
- Database fallbackToDestructiveMigration для beta-версий

---

## v1.0.0-beta.2

### доработка — Killer Features реальны

#### Agents + Memory интеграция с AI
- **Agents** — активные агенты автоматически добавляют системный промпт в каждый чат
- **Memory** — записи памяти (key/value) инжектятся как контекст пользователя в AI
- **AppDataManager** — общий Hilt singleton для agents и memory

#### Исправления UX
- **Chains** — ошибки показываются пользователю (красная карточка), вместо проглатывания
- **SplitView** — результаты сравнения сохраняются в сессии
- **Voice Mode** — полная история диалога отправляется в AI (multi-turn)
- **Marketplace** — при добавлении провайдера появляется поле ввода API Key
- **CodePlayground** — превью авто-обновляется с задержкой 500ms (debounce)

#### Architecture
- Database v2 — agentId в Tab
- AIAgent, MemoryEntry модели в domain module
- TabBar улучшен (swipe-to-dismiss, анимации)

---

## v1.0.0-beta.1

### Beta Release — полный функционал

#### Новые фичи
- **Automation Chains** — цепочки автоматизации: скрипт → видео → превью → YouTube
- **Plugin System** — 6 встроенных плагинов (Git, Docker, Firebase, Code Runner, Export, AI Enhancer)
- **Local AI Support** — подключение Ollama, llama.cpp, LM Studio через localhost
- **Team Workspaces** — совместная работа с WebSocket чатом и ролями участников
- **Biometric Lock** — вход по отпечатку/лицу с настройкой в Settings
- **Реальные экраны** — Image, Video, Agents, Memory вместо заглушек

#### Улучшения
- **ErrorHandler** — централизованная обработка ошибок с user-friendly сообщениями
- **ResponseCache** — кэширование ответов AI для offline-режима
- **Тесты** — 99+ тестов покрывают все новые модули
- **Performance** — Lazy loading, image caching (Coil)

#### Модули
- feature:localai — Ollama, llama.cpp, LM Studio, LocalAI
- feature:teamworkspaces — WebSocket чат, роли, workspace

#### Исправления
- Исправлен импорт ChatMessage (domain.ai вместо domain.model)
- Исправлен AIProviderIcon параметр (providerId вместо providerType)
- Исправлен MainActivity → FragmentActivity для BiometricPrompt
- Исправлен FlowRow @OptIn для ExperimentalLayoutApi

---

## v1.0.0-alpha.1

### First Alpha Release

#### Core Features
- **Multi-tab Workspace** — независимые AI-вкладки с историей сообщений
- **AI Provider System** — поддержка OpenAI, Anthropic, Gemini и кастомных API
- **Streaming Responses** — real-time генерация ответов от AI

#### Killer Features
- **AI Marketplace** — каталог AI-провайдеров с одной кнопкой подключения (10 пресетов)
- **Prompt Library** — 16 готовых промптов в 9 категориях с поиском и избранным
- **Code Playground** — редактор HTML/CSS/JS с live-превью в WebView
- **Module System** — управление модулями приложения (включение/выключение)
- **AI Router** — авто-выбор лучшего AI с 5 стратегиями и failover
- **Split View** — сравнение ответов 2-4 AI на один вопрос с рейтингом
- **Voice Mode** — голосовое общение с AI (STT → LLM → TTS)

#### UI/UX
- Material 3 тема с тёмным purple дизайном
- Навигация: bottom bar + drawer + FAB
- Онбординг (4 страницы)
- Экспорт чатов в Markdown, TXT, JSON, HTML
- Файловый менеджер с галереей и фильтрами
- Редактор кода с подсветкой синтаксиса
- Настройки провайдеров с шифрованием API-ключей

#### Architecture
- Kotlin 2.1.0, Jetpack Compose, MVVM + Clean Architecture
- Multi-module: app, core, domain, data, di, feature:tabs, feature:settings, feature:editor, feature:aiprovider
- Hilt DI, Room DB, Ktor HTTP, DataStore Preferences
- GitHub Actions CI/CD (build + test + lint)
