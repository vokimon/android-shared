# android-shared

Shared components for Android kotlin applications

## Usage as Submodule

```
git submodule add git@github.com:vokimon/android-shared.git shared
git submodule update --init --recursive
```

Add to your app's `settings.gradle.kts`:
```kotlin
include(":shared")
```

In your app `build.gradle.kts`
```
dependencies {
    implementation(project(":shared"))
}
```

## As maven library

Planned but not yet uploaded.

## Stability

`shared` is meant for my own personal projects use
and it is tied to their evolution.
Consider all API's unstable meanwhile.

If you decide to use them, make me a line.
I will consider to publish it in Maven,
and with all the goodies of a well mainained library:
semantic versioning, changelog with breaking change notices and a migration guide.

## Content

- usermessage: Decoupled message sending to the snackbar
    - UserMessage: Decoupled message sender
    - UserMessageSnackbarHost: Message receiver showing an SnackBar message
- settings: Settings manager for Theme and Language
    - LanguageSettings
    - ThemeSettings
- crash: The crash reporter
    - CrashReporter: To be initialized on the Application object
    - CrashDialog: To be inserted in the main activity
    - XXXCrashBackend: Pluggable report actions (GitHub, Share, Copy, Save...)
- storage: Compose wrappers for MediaStore and SAF (File Pickers)
    - rememberOpenFilePicker
    - rememberSaveFilePicker
    - rememberSaveToMediaStore
- component/
    - preference: Reactive access to Preferences and compose version of PreferenceViews
        - ReactivePreference: Reactive access to SharedPreferences
            - rememberPreferenceState: read-write
            - rememberPreferenceValue: read-only
        - PreferenceCategory: Category separator
        - ListPreference: Option chooser
        - SwitchPreference: Boolean chooser
        - LinkPreference: Clickable item to visit a link
        - WeblateLink: Link inviting to translate on Weblate
    - AppScaffold: Top level activity widget with all the whistles
    - OneTimeNotice: Dialogs that appear only once
    - StackNavigator: Simplified stack based navigation
    - AsyncList: A lazy list loading async content
    - WatermarkBox: Container Box with an icon as watermark
    - ContextExtensions: Convenience context extensions to start uris and intents
    - Flow: Column or Row depending on a boolean
    - LazyColumn: Adds border shades as scrollability indicators to the standard material3 one
    - Scrollbar: Always visible scrollbar


## License

Copyright © 2025-2026 David García Garzón

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

