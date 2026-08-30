# GitHub Project Review Prompt for NexsusAI

Use this prompt to analyze and review the NexsusAI Android project on GitHub.

---

## 🔍 Comprehensive Repository Analysis Prompt

```
Analyze the NexsusAI Android project repository at [REPO_URL].

### 1. Project Structure & Architecture
- Verify Clean Architecture + MVVM implementation across modules (app, core, domain, data, feature, di)
- Check module dependencies direction (domain → data, feature → domain, etc.)
- Validate Hilt DI setup and component hierarchy
- Confirm navigation graph with bottom bar + drawer menu

### 2. Code Quality & Standards
- Kotlin style guide compliance (ktlint/detekt)
- Proper use of Coroutines + Flow for async operations
- Error handling patterns (Result/Either/Sealed classes)
- Memory leak prevention (ViewModel scoping, lifecycle awareness)
- Resource management (coroutines cancellation, disposable cleanup)

### 3. Build & CI/CD (GitHub Actions)
- android.yml: Build, test, lint on push/PR to main
- release.yml: Release automation
- Gradle version catalog usage
- Dependency freshness (check for outdated libs)
- Build cache configuration

### 4. Testing Strategy
- Unit tests coverage (ViewModels, UseCases, Repositories)
- Integration tests (Room DAOs, API clients)
- UI tests (Compose testing)
- Test fixtures and fakes for AI providers
- CI test execution verification

### 5. Android-Specific Checks
- Min/Target/Compile SDK versions
- Jetpack Compose version alignment
- Material 3 + Dynamic Color implementation
- Proper permissions declaration
- ProGuard/R8 rules for release
- App signing configuration

### 6. Security & Privacy
- API key encryption (Android Keystore)
- No secrets in code/git history
- Network security config
- Biometric auth implementation
- Data encryption at rest (SQLCipher/Room encryption)

### 7. Feature Implementation Status
- [ ] Stage 1: Architecture & Scaffold
- [ ] Stage 2: Tabs Manager (Room, swipeable tabs, compare mode)
- [ ] Stage 3: AI Provider System (interface, config, encryption)
- [ ] Stage 4: Main UI (Material 3, tabs, chat area, bottom nav)
- [ ] Stage 5: Content Editor (rich text, code, images, files)
- [ ] Stage 6: Provider Settings Screen
- [ ] Stage 7: Task Templates
- [ ] Stage 8: File Manager & Media
- [ ] Stage 9: Export & Integrations
- [ ] Stage 10: Polish (performance, UX, privacy, accessibility)

### 8. Documentation
- README with setup, build, run instructions
- Architecture decision records (ADRs)
- API documentation (KDoc)
- Contributing guidelines
- Changelog

### 9. Performance & Optimization
- Lazy loading for inactive tabs
- Image caching (Coil) configuration
- Pagination for chat history
- Background sync implementation
- APK size analysis
- Startup time metrics

### 10. Accessibility & Internationalization
- TalkBack support
- Font scaling
- High contrast mode
- RTL support
- String externalization

---

### Expected Output Format

Provide a structured report with:

1. **Executive Summary** - Overall health score (1-10)
2. **Critical Issues** - Blockers for release
3. **Major Issues** - Significant technical debt
4. **Minor Issues** - Code style, optimizations
5. **Positive Findings** - Well-implemented areas
6. **Recommendations** - Prioritized action items
7. **Stage Completion Status** - % complete per stage
```

---

## 🎯 Quick Health Check Prompt (Short Version)

```
Quick review of NexsusAI repo at [REPO_URL]:

1. Does ./gradlew build pass locally?
2. Do all tests pass (./gradlew test)?
3. Does lint pass (./gradlew lint)?
4. Is CI green on main branch?
5. Are there any open security alerts?
6. What's the test coverage %?
7. Any outdated critical dependencies?
8. Stage 2 (Tabs) implementation status?
```

---

## 🔧 Automated Verification Commands

Run these locally to verify before review:

```bash
# Full build verification
./gradlew build test lint

# Dependency check
./gradlew dependencyUpdates

# Security scan
./gradlew dependencyCheckAnalyze

# APK analysis
./gradlew assembleRelease && ./gradlew bundleRelease
```