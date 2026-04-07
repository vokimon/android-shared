# android-shared

Shared components for Android kotlin applications

## Usage as Submodule

When included as a submodule, the following files are ignored:
- `settings.gradle.kts` (use app's settings)
- `gradle/wrapper/` (use app's wrapper)

Add to your app's `settings.gradle.kts`:
```kotlin
include(":shared")
project(":shared").projectDir = file("./shared")



