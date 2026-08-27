# NexsusAI v0.2.4 AI Workspace

## Goal
Create an AI workspace inside each independent tab.

## Features
- Chat interface per session
- Session context storage
- AI model selection
- Unified AI Provider API

## Architecture

Compose UI
 -> AI Workspace ViewModel
 -> AI Provider Manager
 -> OpenAI / Claude / Local Models

## Providers
Initial abstraction supports:
- OpenAI
- Anthropic Claude
- Local models
- Custom providers

## Next steps
- Implement chat message persistence
- Add provider implementations
- Add API key management
