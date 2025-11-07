# O2 ScratchCard – Android Home Assignment

A simple Android application written in **Kotlin + Jetpack Compose (MVVM)** that models a scratch card lifecycle:
- Initially **unscratched**
- Then **scratched** (revealing a random UUID code)
- And finally **activated** via an API call

---

## Features

- **Three screens:**
    - `MainScreen` — displays current card state and navigation buttons
    - `ScratchScreen` — simulates scratching (2-second delay, reveals code)
    - `ActivationScreen` — activates card via remote API

- **API Integration (Retrofit):**
  GET https://api.o2.sk/version?code=<uuid>

→ If `android` value in response is **greater than 277028**,  
the card becomes *activated*. Otherwise, an error dialog appears.

- **MVVM architecture** with shared `ViewModel` and `StateFlow`
- **Coroutines** for async operations and cancellation handling
- **Material3 + Jetpack Compose Navigation**
- **Unit tests** for core logic (ViewModel)
- **Local in-memory history** of all scratch and activation actions

---

## Architecture Overview

```
com.matejfilus.o2scratchcard/
│
├── data/
│ ├── api/
│ │ ├── ActivationApi.kt
│ │ └── RetrofitInstance.kt
│ └── repository/
│    ├── ActivationRepository.kt
│    └── DefaultActivationRepository.kt
│
├── domain/
│ └── model/
│    ├── CardState.kt
│    ├── ScratchCard.kt
│    └── CardHistoryItem.kt
│
├── ui/
│ ├── main/ → MainScreen.kt
│ ├── scratch/ → ScratchScreen.kt
│ ├── activation/ → ActivationScreen.kt
│ └── theme/ → default Compose theme
│
└── viewmodel/
  ├── ScratchCardViewModel.kt
  └── ScratchCardViewModelFactory.kt
```
---

## Tech Stack

| Category        | Library                        |
|-----------------|--------------------------------|
| **UI**          | Jetpack Compose, Material 3     |
| **Architecture**| MVVM, StateFlow                 |
| **Navigation**  | androidx.navigation.compose     |
| **Networking**  | Retrofit 2 + Gson               |
| **Async**       | Kotlin Coroutines               |
| **Testing**     | JUnit, Coroutine Test           |

---

## App Flow

**Main screen**  
Displays current card state and buttons to navigate.

**Scratch screen**  
Simulates a heavy operation (2s delay).  
Generates a random UUID and updates card state to `SCRATCHED`.

**Activation screen**  
Sends the generated code to API.  
If response → `"android": "287028"` (value > 277028),  
the card becomes `ACTIVATED`, otherwise an error dialog appears.

All state is shared via `ScratchCardViewModel`.

---

## Unit Tests

- Tests focus on **ViewModel** logic:
    - Scratching updates state and generates UUID
    - Activation changes state only on valid API response
    - Error handling for failed activations

*(Example tests in `ScratchCardViewModelTest.kt`)*

---

## How to Run

1. Open project in **Android Studio (Giraffe or newer)**
2. Sync Gradle (`File → Sync Project with Gradle Files`)
3. Run the app on an emulator or physical device (API 24+)

---


## Author

**Matej Filus**  
🧠 Android Developer Candidate  
📍 O2 Slovakia Home Assignment

---

## License

This project was created as part of a technical interview assignment for O2 Slovakia.  
It is not intended for production use.