# NexsusAI v0.2.5 AI Real Providers

## Goal
Connect real AI backends through one streaming interface.

## Providers
- OpenAI API
- Anthropic Claude API
- Local models (Ollama/embedded runtime preparation)

## Security
API keys are isolated behind ApiKeyStorage. Implementation target: Android Keystore + encrypted storage.

## Streaming
Responses are delivered through Kotlin Flow for token-by-token UI updates.

Architecture:

AI Workspace
 -> StreamingAIProvider
 -> Provider Adapter
 -> API / Local Runtime
