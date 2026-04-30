# Moisesai Challenge

A music player Android app built with Jetpack Compose that searches tracks via the iTunes API, plays them with a media notification, and lets users organize playlists.

---

## Features

### Search Pagination
The iTunes API does not support server-side pagination — it returns all results in a single response. To work around this, the app requests the maximum allowed result count (200 tracks) on the first search and stores them in an in-memory cache. The UI then paginates by slicing that cached list, so page 2, 3, etc. never trigger a new network request.

Paging 3 would have been a natural fit here, but it introduces a significant amount of boilerplate (PagingSource, RemoteMediator, LazyPagingItems) for a benefit that is largely invisible to the user given the iTunes API constraint. The custom slice-based approach achieves the same UX with far less ceremony.

### Cache
All caches (search results, albums, recently played) are held in memory. This means the cache is lost when the app is killed and re-opened. The upside is that any data already fetched is available completely offline until the process dies.

### Media Notification
While a track is playing, a persistent notification appears in the status bar showing the track name and artwork, with previous, play/pause, and next controls. This is backed by a foreground `MediaSessionService` so the notification survives the app going to the background.

### Online / Offline Banner
A banner is shown at the top of the screen whenever the device loses connectivity. It disappears automatically when the connection is restored. Network errors caused by connectivity loss are silently suppressed in the UI (no snackbar), since the banner already communicates the problem.

### Tablet Layout
On wide screens (expanded window width class), the app switches to a two-pane layout: the main content on the left and the player on the right, visible at all times without needing to navigate to a separate screen. I took some liberties in the design on this feature.

### Playlists
Users can create, edit, and delete playlists. Tracks can be added to a playlist from the search results or album screen. Everything is stored in memory, so playlists do not survive app restarts.

### Queue
The player does not just play the selected track in isolation. Tapping a track in search results queues the full result list starting from that track. The same applies to albums and playlists — the entire list becomes the queue, preserving the listening context and allowing skip forward/back to work naturally.

### Error Handling
Non-connectivity errors (unexpected API failures, server errors) surface as a snackbar at the bottom of the screen with the error message. Connectivity errors are handled separately by the offline banner and are not shown as snackbars to avoid redundant messaging.

### Pull to Refresh
On the search results screen, pulling down resets the pagination to page 1 and re-fetches from the repository. The existing results stay visible while the refresh is in progress, so there is no jarring blank-screen flash.

### Chinese Localization
All UI strings are localized into Simplified Chinese (`values-zh/strings.xml`). This was introduced as an easter egg after being told about plans to expand into the Chinese market.

---

## Architecture

The app follows **MVVM + Clean Architecture** with three layers:

- **Domain** — plain Kotlin models and repository interfaces. No Android dependencies.
- **Data** — repository implementations, local in-memory data sources, and a Retrofit-based remote data source talking to the iTunes Search API.
- **Presentation** — Jetpack Compose screens, one ViewModel per screen, and a singleton `PlaybackManager` that owns the `MediaPlayer` state and exposes it as `StateFlow`s.

Use cases were deliberately omitted. In this codebase every use case would have been a single-method wrapper that simply delegates to a repository — pure boilerplate with no logic of its own. ViewModels call repositories directly instead.

Dependency injection is handled by **Hilt**.

---

## Package Structure

```
com.example.moisesaichallenge
├── core
│   ├── network          # Retrofit client, NetworkResult
│   └── playback         # PlaybackManager, PlaybackService
├── data
│   ├── local            # In-memory data sources (tracks, albums, recently played, playlists)
│   ├── network          # iTunes API service + DTOs
│   └── repository       # Repository implementations
├── domain
│   ├── model            # Domain models (Track, Album, Playlist, …)
│   └── repository       # Repository interfaces
├── navigation           # Type-safe route definitions
└── presentation
    ├── album
    ├── components       # Shared composables (TrackItem, banners, bottom sheets)
    ├── home
    │   ├── playlists
    │   └── search
    ├── player
    ├── splash
    └── tablet
```

---

## Tests

Unit tests cover all local data sources, remote data sources, repository implementations, and ViewModels.

- **MockK** is used for mocking. It handles both regular and suspending functions cleanly and has an expressive DSL that reads close to plain English.
- **Turbine** is used to test `StateFlow` and `SharedFlow`. `testIn(backgroundScope)` is used for `SharedFlow` (replay = 0) to subscribe before the emission is triggered and avoid missing events. `StateFlow` assertions use `.test { expectMostRecentItem() }`.
- **`StandardTestDispatcher`** is used for tests that involve time-based logic (the 300 ms search debounce), combined with `advanceTimeBy`.

`SavedStateHandle.toRoute<T>()` was replaced with `savedStateHandle.get<Long>("key")` in all ViewModels that read navigation arguments. `toRoute` internally calls Android's `Bundle` API which is not available in JVM unit tests.

---

## What Could Be Improved

- **UI polish** — several screens could follow Material Design guidelines more closely (spacing, typography scale, touch target sizes).
- **Persistent storage** — replacing the in-memory stores with a Room database would make search history, playlists, and the album cache survive app restarts.
- **Tablet playback** — during emulator testing the player was not producing audio on the tablet layout. It is unclear whether this is an emulator audio routing issue or a bug in the two-pane setup. There was not enough time to debug it properly.
