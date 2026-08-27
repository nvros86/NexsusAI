# NexsusAI v0.2.3 Tabs UI

## Goal
Implement persistent AI workspace tabs.

Features:
- create independent AI sessions;
- switch active sessions;
- close sessions;
- persist changes through Room Database.

Flow:

UI -> ViewModel -> SessionRepository -> Room Database

Each tab represents an isolated AI workspace context and is ready for future AI Provider integration.
