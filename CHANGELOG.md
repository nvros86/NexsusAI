# Changelog

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
