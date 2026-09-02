# Changelog

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
