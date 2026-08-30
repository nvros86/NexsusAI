# Changelog

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
