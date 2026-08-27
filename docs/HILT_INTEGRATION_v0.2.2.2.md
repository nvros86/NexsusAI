# NexsusAI v0.2.2.2 Hilt Integration

## Goal
Connect Room Database and SessionRepository through Hilt dependency injection.

## Added
- AppModule provides AppDatabase.
- SessionRepository is provided as a singleton dependency.
- Prepared foundation for injecting repository into ViewModel.

## Flow
Room Database -> DAO -> SessionRepository -> ViewModel -> Compose UI
