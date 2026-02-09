# News App with Clean Architecture

A native Android news application built with Jetpack Compose and Clean Architecture, powered by the [NewsAPI.org](https://newsapi.org/) API.

LLMs used: Claude Code

## Features

- **Top Headlines** — Horizontal scrollable list with infinite scroll (auto-recycles when API data is exhausted)
- **Indonesia News** — Vertical list fetching from the `/everything` endpoint with infinite scroll
- **Search** — Dedicated search mode using the `/everything` endpoint, shown in a separate results view
- **Collapsible Search Bar** — Hides on scroll down, slides back in on scroll up as an overlay
- **Offline Caching** — Top headlines are cached locally via Room for offline access
- **Placeholder Images** — Fallback image displayed when an article has no image

## Tech Stack

| Category | Library |
|---|---|
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Networking | Retrofit, OkHttp, Gson |
| Local DB | Room |
| Image Loading | Coil |
| Navigation | Navigation Compose |
| State Management | StateFlow, Compose State |

## Architecture

The project follows **Clean Architecture** with three distinct layers:

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│  (Compose UI, ViewModel, UiState)           │
├─────────────────────────────────────────────┤
│                Domain Layer                 │
│  (Use Cases, Repository Interface, Model)   │
├─────────────────────────────────────────────┤
│                 Data Layer                  │
│  (Repository Impl, API Service, Room DB,    │
│   DTOs, Entities, Mappers)                  │
└─────────────────────────────────────────────┘
```

Dependencies point inward — the Domain layer has no Android dependencies, and the Presentation layer depends on Domain but never on Data directly.

## Project Structure

```
com.mhibrizif.news/
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── ArticleDao.kt            # Room DAO for cached articles
│   │   ├── entity/
│   │   │   └── ArticleEntity.kt          # Room entity with category and cache timestamp
│   │   └── NewsDatabase.kt              # Room database definition
│   ├── remote/
│   │   ├── dto/
│   │   │   ├── ArticleDto.kt            # API response article model
│   │   │   ├── NewsResponseDto.kt       # API response wrapper
│   │   │   └── SourceDto.kt             # API response source model
│   │   └── NewsApiService.kt            # Retrofit service (top-headlines, everything)
│   ├── mapper/
│   │   └── NewsMapper.kt                # DTO <-> Domain <-> Entity mapping functions
│   └── repository/
│       └── NewsRepositoryImpl.kt        # Repository with caching and fallback logic
│
├── domain/
│   ├── model/
│   │   └── Article.kt                   # Domain model (clean, no framework deps)
│   ├── repository/
│   │   └── NewsRepository.kt            # Repository interface (contract)
│   └── usecase/
│       ├── GetTopHeadlinesUseCase.kt     # Fetch paginated top headlines
│       ├── GetIndonesiaNewsUseCase.kt    # Fetch Indonesia news via /everything
│       └── SearchNewsUseCase.kt          # Search news via /everything
│
├── presentation/
│   └── news/
│       ├── components/
│       │   ├── ArticleCard.kt            # Vertical list card (image, title, desc, author)
│       │   └── HeadlineCard.kt           # Horizontal list card (compact, larger title)
│       ├── NewsScreen.kt                 # Main screen composable with overlay search bar
│       ├── NewsUiState.kt                # UI state data class
│       └── NewsViewModel.kt             # ViewModel with pagination and infinite scroll
│
├── di/
│   ├── NetworkModule.kt                  # Provides Retrofit, OkHttp, API service
│   ├── DatabaseModule.kt                # Provides Room database and DAO
│   └── RepositoryModule.kt             # Binds repository implementation
│
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
│
├── MainActivity.kt                       # Hilt entry point, hosts Compose content
└── NewsApplication.kt                   # Hilt application class
```

## Data Flow

```
User Action
    │
    ▼
NewsScreen (Compose)
    │
    ▼
NewsViewModel (StateFlow)
    │
    ▼
Use Case (GetTopHeadlines / GetIndonesiaNews / SearchNews)
    │
    ▼
NewsRepository (interface)
    │
    ▼
NewsRepositoryImpl
    ├── API call via NewsApiService (Retrofit)
    ├── Cache to Room (top headlines only)
    └── Fallback to cached data on failure
    │
    ▼
Result<List<Article>> flows back up to UI
```

## Screen Layout

```
┌──────────────────────────────┐
│  TopAppBar: "News"           │
├──────────────────────────────┤
│  Search Bar (overlay)        │  ← hides on scroll down,
│                              │    slides in on scroll up
├──────────────────────────────┤
│  Top Headlines               │  ← section label
│  ┌──────┐ ┌──────┐ ┌──────┐ │
│  │ Card │ │ Card │ │ Card │→ │  ← horizontal LazyRow
│  └──────┘ └──────┘ └──────┘ │    (infinite scroll)
├──────────────────────────────┤
│  Indonesia                   │  ← section label
│  ┌──────────────────────────┐│
│  │ ArticleCard              ││
│  └──────────────────────────┘│  ← vertical LazyColumn
│  ┌──────────────────────────┐│    (infinite scroll)
│  │ ArticleCard              ││
│  └──────────────────────────┘│
│           ...                │
└──────────────────────────────┘
```

When searching, the headlines and Indonesia sections are replaced with a "Search Results" vertical list.

## API Endpoints Used

| Endpoint | Usage |
|---|---|
| `GET /v2/top-headlines?country=us&page=&pageSize=` | Top Headlines horizontal list |
| `GET /v2/everything?q=Indonesia&page=&pageSize=` | Indonesia news vertical list |
| `GET /v2/everything?q=&sortBy=publishedAt` | Search results |

## Build Configuration

- **compileSdk:** 35
- **minSdk:** 24
- **targetSdk:** 35
- **Java/Kotlin target:** 11

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. The API key is preconfigured in `build.gradle.kts`
4. Build and run on a device or emulator
