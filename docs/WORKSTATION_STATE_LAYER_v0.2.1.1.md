# NexsusAI Workstation State Layer v0.2.1.1

## Added

- Reactive UI state model
- ViewModel layer
- StateFlow based session updates
- Active session selection

## Architecture

Compose UI
↓
WorkstationViewModel
↓
WorkstationUiState
↓
SessionManager
↓
Session Repository

## Goal

Prepare NexsusAI Workstation for persistent AI sessions, multiple providers and independent agent contexts.
