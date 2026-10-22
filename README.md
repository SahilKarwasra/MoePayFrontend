# MoePay (Android + iOS)

Compose Multiplatform client for [MoePay-Backend](../MoePay-Backend). Multi-module clean
architecture (see `.claude/skills/cmp-clean-architecture`), monochrome design system, and
platform-native chrome: on iOS the top bar, tab bar and alerts are real UIKit controls
(`UINavigationBar`, `UITabBar`, `UIAlertController`); on Android they are Material 3.

## Modules

```
build-logic/convention     Gradle convention plugins (kmp.library, cmp.library, cmp.feature,
                           cmp.application, android.application, app.config)
androidApp                 Android entry point: MoePayApplication (Koin), MainActivity (system splash)
iosApp                     Xcode project: SwiftUI shell hosting MainViewController, launch screen, icon
shared                     Composition root: App, MainViewModel, Splash, NavigationRoot, initKoin
core/domain                Result/DataError, AuthInfo, SessionStorage, AuthService, OnboardingStep
core/data                  Ktor client (envelope parsing, bearer + refresh), DataStore session, AppConfig
core/presentation          UiText, ObserveAsEvents, DataError -> message
core/designsystem          MoeTheme, typography, MoeButton/TextField/OtpField/Logo/...,
                           native/ (MoeTopBar, MoeNavigationBar, MoeAlertDialog: expect/actual)
feature/auth               welcome -> phone number -> OTP          (domain, presentation)
feature/onboarding         profile -> username -> review           (domain, data, presentation)
feature/home               empty tabs + logout                      (presentation)
```

Dependency rule: `presentation` and `domain` never import Ktor, DataStore or DTOs; `data`
implements domain interfaces; features never depend on each other.

## Flow

Splash (reads the stored session) → **Auth** if signed out, **Onboarding** at the backend's
`next_step` if not finished, otherwise **Home**. Logging out or a rejected refresh token clears
the session and `MainViewModel` sends the app back to Auth.

## Running

Start the backend first (`make run` in MoePay-Backend → `http://localhost:8080`). In development
the OTP is printed in the backend log (`DEV ONLY: OTP`).

Base URLs default to the local backend and can be overridden in `local.properties`:

```properties
MOEPAY_BASE_URL_ANDROID=http://10.0.2.2:8080
MOEPAY_BASE_URL_IOS=http://localhost:8080
```

- Android: `./gradlew :androidApp:assembleDebug`, or run `androidApp` from Android Studio.
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme.

## Adding a feature

1. `feature/<name>/{domain,data,presentation}` + `include(...)` in `settings.gradle.kts`.
2. Plugins: `convention.kmp.library` for domain/data, `convention.cmp.feature` for presentation.
3. Screens follow `XRoot` (ViewModel, events, navigation lambdas) + stateless `XScreen(state, onAction)`.
4. Register the Koin module in `shared/.../di/initKoin.kt` and the graph in `NavigationRoot`.
