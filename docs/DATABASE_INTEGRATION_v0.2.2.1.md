# NexsusAI v0.2.2.1 Database Integration

## Goal
Connect Room persistence with the Workstation state layer.

## Added components

- AppDatabase - Room database entry point.
- SessionRepository - abstraction between storage and ViewModel.

## Data flow

Room Database
-> DAO
-> SessionRepository
-> ViewModel
-> StateFlow
-> Compose UI

## Next steps

- Add Hilt dependency injection.
- Connect Repository to WorkstationViewModel.
- Restore saved AI sessions on application startup.
