# NexsusAI v0.2.5.3 — Ktor + SSE Streaming Implementation

## Цель
Подготовка потокового канала ответов AI через Ktor Client и Server-Sent Events.

## Архитектура

AIWorkspaceScreen
↓
AIWorkspaceViewModel
↓
AIProviderManager
↓
Ktor HTTP Client
↓
SSE Stream
↓
Flow<String>

## Компоненты

- Ktor Client для HTTP-запросов
- SSE parser для потоковых событий
- безопасное получение API ключей через ApiKeyStorage
- передача токенов в Compose UI

## Следующий шаг
Полная реализация адаптеров OpenAI и Anthropic с реальными endpoint вызовами.
