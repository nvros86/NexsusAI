# AI Workspace Pro — Промт разработки

## Общая концепция

Мульти-AI рабочее окружение для Android — каждый AI работает в своей вкладке, независимо, с поддержкой файлов, изображений, видео и генерации контента.

---

## ЭТАП 1: Архитектура и каркас проекта

```
Создай Android приложение (Kotlin, Jetpack Compose) с архитектурой MVVM + Clean Architecture.

Структура проекта:
- app/
  - core/          → общие утилиты, базы данных, сеть
  - domain/        → бизнес-логика, модели, репозитории
  - data/          → API-клиенты, локальные источники данных
  - feature/       → модули фичей (tabs, settings, editor, ai-provider)

Используй:
- Hilt для DI
- Room для локальной БД
- Retrofit + OkHttp для сетевых запросов
- DataStore для настроек
- Coroutines + Flow для асинхронности

Создай навигационный graph с bottom bar и drawer menu.
```

---

## ЭТАП 2: Система вкладок (Tabs Manager)

```
Реализуй систему вкладок как в браузере:

- Каждая вкладка = отдельный ViewModel с собственным AI-провайдером
- Вкладки хранятся в Room DB (TabEntity: id, title, aiProviderId, messages, createdAt)
- Swipable tabs в顶部 с кнопкой "+"
- Каждая вкладка имеет:
  * Свой AI-провайдер (настраивается отдельно)
  * Историю сообщений
  * Контекст (загруженные файлы, изображения)
  * Состояние (загрузка, ошибка, готово)

- Long-press на вкладку → контекстное меню (дублировать, переименовать, закрыть, закрыть все)
- Swipe для закрытия вкладки
- Режим "Compare mode" — показать 2 вкладки рядом (split view)

Data class TabState:
  id: String
  title: String
  aiProviderId: String?
  messages: List<Message>
  attachedFiles: List<AttachedFile>
  isActive: Boolean
  createdAt: Long
```

---

## ЭТАП 3: Провайдеры AI (AI Provider System)

```
Создай универсальную систему добавления AI-провайдеров:

interface AIProvider {
    val id: String
    val name: String
    val icon: Int
    val models: List<AIModel>
    suspend fun sendMessage(messages: List<Message>, model: String): Flow<String>
    suspend fun generateImage(prompt: String, params: ImageParams): Uri?
    suspend fun uploadFile(file: Uri): String
}

Data class AIProviderConfig:
    id: String
    name: String (user-defined, например "My GPT-4")
    type: ProviderType (OPENAI, ANTHROPIC, GEMINI, CUSTOM, LOCAL, HUGGINGFACE)
    baseUrl: String
    apiKey: String (encrypted in DB)
    models: List<String>
    defaultModel: String
    maxTokens: Int
    temperature: Float
    systemPrompt: String
    customHeaders: Map<String, String>
    supportsImages: Boolean
    supportsFiles: Boolean
    supportsStreaming: Boolean
    rateLimit: RateLimitConfig

Поддерживаемые типы из коробки:
- OpenAI (GPT-4o, DALL-E, Whisper)
- Anthropic (Claude)
- Google Gemini
- Stability AI (генерация изображений)
- ElevenLabs (озвучка)
- RunwayML / Pika (видео)
- Custom endpoint (любой API)

Реализуй шифрование API-ключей через Android Keystore.
```

---

## ЭТАП 4: UI — Главный экран и дизайн

```
Создай modern UI в стиле Material 3 Dynamic Color:

Главный экран:
┌─────────────────────────────────┐
│ ≡  AI Workspace Pro        ⚙️  │  ← Top app bar
├─────────────────────────────────┤
│ [Tab1] [Tab2] [Tab3] [+]       │  ← Scrollable tabs
├─────────────────────────────────┤
│                                 │
│     Chat / Workspace Area       │  ← Основная область
│                                 │
│  ┌──────────────────────────┐   │
│  │ 🤖 AI Response...       │   │
│  └──────────────────────────┘   │
│  ┌──────────────────────────┐   │
│  │ 💬 User message          │   │
│  └──────────────────────────┘   │
│                                 │
├─────────────────────────────────┤
│ 📎 🖼️ 🎬  [Message input...]  │  ← Input bar
│                    [Send ➤]     │
└─────────────────────────────────┘
│ 🏠  💬  🛠️  📁  ⚙️           │  ← Bottom nav
└─────────────────────────────────┘

Цветовая схема: темная тема по умолчанию с акцентными цветами.
Каждая вкладка может иметь свой цвет (для визуального отличия).
Анимации: shared element transitions, ripple effects, smooth scrolling.
```

---

## ЭТАП 5: Редактор контента (Content Editor)

```
Создай встроенный редактор с поддержкой разных типов контента:

Типы сообщений:
- TextMessage (markdown рендеринг)
- CodeMessage (syntax highlight, копирование, запуск)
- ImageMessage (предпросмотр, зум, шаринг)
- VideoMessage (встроенный плеер)
- FileMessage (скачивание, предпросмотр)
- GeneratedContent (HTML/CSS/JS preview, Markdown preview)
- AudioMessage (плеер для озвучки)

UI редактора:
- Rich text input с toolbar (bold, italic, code, lists)
- Кнопка прикрепления файлов (image picker, video picker, document picker)
- Drag & drop зона для файлов
- Clipboard paste для изображений
- Кнопка голосового ввода (Whisper API)

Функции:
- "Use as context" — прикрепить файл как контекст для AI
- "Generate from this" — отправить файл на анализ
- Inline preview для HTML/CSS/JS кода
- Diff view для сравнения версий
```

---

## ЭТАП 6: Расширенные настройки AI

```
Создай экран настроек для каждого AI-провайдера:

Provider Settings Screen:
┌─────────────────────────────────┐
│ ← AI Provider Settings          │
├─────────────────────────────────┤
│ Name: [My Custom GPT]           │
│ Type: [OpenAI       ▼]          │
│ Base URL: [https://api...  ]    │
│ API Key: [•••••••••] 👁️         │
│                                  │
│ ── Models ──                     │
│ ☑ gpt-4o                         │
│ ☑ gpt-4o-mini                    │
│ ☐ gpt-3.5-turbo                  │
│ [+ Add custom model]             │
│                                  │
│ ── Parameters ──                 │
│ Default Model: [gpt-4o     ▼]   │
│ Max Tokens: [4096]    [slider]  │
│ Temperature: [0.7]    [slider]  │
│ System Prompt:                     │
│ ┌────────────────────────────┐   │
│ │ You are a helpful...       │   │
│ └────────────────────────────┘   │
│                                  │
│ ── Capabilities ──               │
│ ☑ Text generation               │
│ ☑ Image generation              │
│ ☑ Code generation               │
│ ☑ File analysis                  │
│ ☑ Voice                          │
│ ☑ Video generation               │
│                                  │
│ ── Advanced ──                   │
│ Custom Headers (JSON)            │
│ Rate Limit: [30] req/min        │
│ Timeout: [30] seconds            │
│ Retry on failure: ☑              │
│                                  │
│ [Test Connection]  [Save]        │
└─────────────────────────────────┘

Также добавь:
- Preset шаблоны (OpenAI, Claude, Gemini пресеты)
- Импорт/экспорт настроек провайдеров
- Группировка провайдеров по категориям
- Pin favorite providers
```

---

## ЭТАП 7: Система шаблонов задач (Task Templates)

```
Создай систему готовых шаблонов для разных задач:

Task Templates:
1. 🎬 Video Creation
   - YouTube video script
   - TikTok/Reels script
   - Video editing instructions
   - Voiceover generation

2. 🖼️ Thumbnail Design
   - YouTube thumbnail
   - Blog post header
   - Social media banner
   - Product image

3. 🌐 Web Development
   - Landing page
   - Portfolio site
   - E-commerce page
   - Blog template

4. 📱 App Development
   - Android app scaffold
   - Flutter widget
   - React Native component
   - UI/UX design

5. ✍️ Content Writing
   - Blog post
   - Email
   - Ad copy
   - Technical documentation

Каждый шаблон:
- Pre-configured system prompt
- Recommended AI provider
- Input fields (parameters)
- Output format settings
- Example prompt

UI: Grid/список шаблонов с иконками, один клик для начала работы.
```

---

## ЭТАП 8: Файловый менеджер и медиа

```
Реализуй встроенный файловый менеджер:

- Внутреннее хранилище проектов (каждая вкладка = проект)
- Поддержка типов файлов:
  * Изображения: JPG, PNG, WebP, SVG
  * Видео: MP4, WebM
  * Аудио: MP3, WAV, OGG
  * Документы: PDF, DOC, TXT, MD
  * Код: JS, Python, HTML, CSS, JSON
  * Архивы: ZIP

- Галерея загруженных файлов
- Файлы как контекст для AI
- Скачивание сгенерированных файлов
- Шаринг файлов через систему share
- Превью файлов (inline)

Storage quota per tab.
```

---

## ЭТАП 9: Экспорт и интеграции

```
Реализуй экспорт результатов:

Экспорт:
- Код → .zip архив с файлами
- Текст → .md / .txt / .docx
- Изображения → .png / .jpg
- Видео → .mp4
- HTML → открыть в браузере / скопировать
- Voice → .mp3

Интеграции:
- Push в GitHub репозиторий
- Деплой на Vercel/Netlify (через API)
- Отправка на YouTube (API)
- Шаринг в соцсети
- Копирование в буфер обмена
- QR-код для быстрой передачи
```

---

## ЭТАП 10: Финальная полировка

```
Финальные штрихи:

Performance:
- Lazy loading для вкладок (unmount inactive tabs)
- Image caching (Coil)
- Pagination для длинных чатов
- Background sync

UX:
- Onboarding wizard
- Tooltips и подсказки
- Keyboard shortcuts (BT keyboard)
- Gesture navigation
- Haptic feedback
- Sound effects (опционально)

Privacy:
- Локальное шифрование всех данных
- Biometric lock
- Incognito mode (не сохранять историю)
- Data export/delete

Accessibility:
- TalkBack support
- Font scaling
- High contrast mode
```

---

## Идеи для конечного продукта

### Название: **NexusAI Studio** или **ForgeAI**

### Killer-features:

1. **AI Marketplace** — каталог готовых AI-провайдеров с пресетами (один клик для добавления)

2. **Team Workspaces** — совместная работа: несколько пользователей делят вкладки и AI-провайдеры (через WebSocket)

3. **Prompt Library** — библиотека промптов с рейтингом, шарингом, категориями

4. **Automation Chains** — цепочки: "Сгенерируй скрипт → Сгенерируй видео → Сгенерируй превью → Загрузи на YouTube"

5. **AI Router** — автоматический выбор лучшего AI для задачи (если один упал — переключает на другой)

6. **Local AI Support** — подключение локальных моделей (Ollama, llama.cpp) через localhost

7. **Split View** — одновременно 2-4 AI отвечают на один вопрос, результаты сравниваются

8. **Code Playground** — встроенный редактор кода с live preview (HTML/CSS/JS)

9. **Voice Mode** — голосовое общение с AI (STT → LLM → TTS)

10. **Plugin System** — плагины для расширения функционала (Git, Docker, Firebase и т.д.)
