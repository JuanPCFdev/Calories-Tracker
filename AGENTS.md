# AGENTS.md — CaloriesTracker

Guía canónica del proyecto para agentes de IA (y humanos). Esta es la ÚNICA fuente de verdad del
proyecto — intencionalmente **no hay CLAUDE.md de proyecto**. Las reglas de proyecto viven acá.
(El `~/.claude/CLAUDE.md` global queda para preferencias personales del dev, sin duplicar reglas de proyecto.)

## Project Overview

App Android de seguimiento calórico con IA. El usuario registra comidas (por foto, texto o búsqueda),
la IA estima macros, y la app lleva el diario diario contra objetivos personales.

- **Package:** `com.juanpcf.caloriestracker`
- **Min SDK:** 27 | **Compile SDK:** 36 | **Target SDK:** 36
- **Lenguaje:** Kotlin | **JVM:** Java 11
- **UI:** Jetpack Compose (sin XML layouts)
- **App en PRODUCCIÓN con ofuscación (R8/ProGuard) habilitada en release.**
- Single-flavor: NO hay product flavors ni multi-cliente.

---

## Architecture

**Clean Architecture en tres capas**, dependencias hacia adentro:

```
feature/   ← Composables (Screen) + ViewModels — solo estado de UI
domain/    ← Use cases, interfaces de repositorio, modelos de dominio
data/      ← Implementaciones de repos, DTOs, mappers, API, Room, Firebase, sync
core/      ← DI (Hilt), navegación, design system, utilidades compartidas
```

**Reglas:**
- La capa `domain/` NO tiene dependencias de Android.
- La capa `feature/` depende SOLO de `domain/` (nunca de `data/` directamente).
- La capa `data/` implementa las interfaces de `domain/`.
- Los **UseCase** son la única entrada de `feature/` a la lógica de `domain/`.
- NO poner lógica de negocio en ViewModels ni en Composables (cálculos, parsing, formateo de
  dominio → `domain/`). Ver *Code Quality*.

---

## Key Libraries & Their Usage

### DI — Hilt
- `@HiltAndroidApp` en `CaloriesTrackerApp` (también `Configuration.Provider` para WorkManager).
- `@AndroidEntryPoint` en `MainActivity` (`AppCompatActivity`).
- `@HiltViewModel` en todos los ViewModels.
- `@HiltWorker` + `@AssistedInject` en Workers.
- Módulos en `core/di/` — seguir el naming `<Cosa>Module.kt`. Bindings de repos vía `@Binds`.

### Networking — Retrofit + OkHttp + kotlinx.serialization
- API de IA en `data/remote/openrouter/` (`OpenRouterApi`).
- DTOs en `data/remote/openrouter/dto/` — **kotlinx.serialization** (`@Serializable` + `@SerialName`),
  NO Gson/Moshi. El converter es `retrofit2-kotlinx-serialization-converter`.
- Modelos LLM gratuitos de OpenRouter; manejo de error/`reasoning_content` ya contemplado en los DTOs.
- API key desde `local.properties` → `BuildConfig.OPENROUTER_API_KEY`. Nunca hardcodear ni commitear.

### Persistencia local — Room
- DB: `CaloriesTrackerDatabase`. DAOs en `data/local/dao/`, entidades en `data/local/entity/`.
- Esquema exportado a `app/schemas/` (`exportSchema = true`, `room { schemaDirectory(...) }`).
- **`Converters` es `@ProvidedTypeConverter`** (tiene que serlo: instancia provista, no creada por Room).
  Por eso TODO `Room.databaseBuilder`/`inMemoryDatabaseBuilder` DEBE encadenar `.addTypeConverter(Converters())`
  — incluido en los tests. Sacarlo NO es una limpieza: Room crashea en runtime con
  "A required type converter ... is missing in the database configuration".
- **Disciplina de migraciones (OBLIGATORIA):** ver *Data & Sync* abajo. Todo cambio de esquema =
  bump `version` + `Migration` + test. NUNCA `fallbackToDestructiveMigration()` en release.

### Sincronización — WorkManager + Firestore
- `FirestoreSyncWorker` (CoroutineWorker periódico cada 15 min + inmediato on-demand).
- Room es la **fuente de verdad**; Firestore es el espejo remoto. Ver *Data & Sync*.

### Auth & Cloud — Firebase
- Firebase Auth (Google Sign-In + email/password). Firestore para perfil, objetivos y diario.
- `auth.currentUser` es confiable sincrónicamente en `onCreate` (el SDK carga la sesión del cache
  antes de `Application.onCreate`). NO depender de un primer emit de `authState` para el start destination.

### Cámara & visión — CameraX + ML Kit
- CameraX para captura; ML Kit barcode para escaneo de productos.

### Imágenes — Coil 3
- `coil-compose` + `coil-network-okhttp`.

### Preferencias — DataStore
- `datastore-preferences` para settings (idioma, tema, etc.).

### Observabilidad — Timber + Firebase Crashlytics
- Timber plantado en `CaloriesTrackerApp`: `DebugTree` en debug; árbol que reporta a Crashlytics en release.
- **NUNCA tragar excepciones en silencio.** Todo `catch` / `runCatching` que no propague DEBE loggear:
  `Timber.e(t)` + `FirebaseCrashlytics.getInstance().recordException(t)` para non-fatals.

---

## Naming Conventions

| Capa | Sufijo | Ejemplo |
|---|---|---|
| ViewModel | `ViewModel` | `AddFoodViewModel` |
| Screen (Composable) | `Screen` | `DiaryScreen` |
| Use Case | `UseCase` | `AddDiaryEntryUseCase` |
| Repository (interfaz) | `Repository` | `DiaryRepository` |
| Repository impl | `RepositoryImpl` | `DiaryRepositoryImpl` |
| API (interfaz) | `Api` | `OpenRouterApi` |
| DTO | `Dto` / `Request` / `Response` | `ChatRequest`, `ChatResponse` |
| Modelo de dominio | (sin sufijo) | `DiaryEntry`, `Food` |
| Entity (Room) | `Entity` | `DiaryEntryEntity` |
| DAO | `Dao` | `DiaryEntryDao` |
| Mapper | extensión `toX()` | `toDomain()`, `toEntity()` |
| Módulo DI | `Module` | `DatabaseModule` |
| Estado de UI | `UiState` | `GoalsUiState` |
| Evento one-shot | `UiEvent` | `AiResultUiEvent` |

---

## State Pattern (ViewModels)

Patrón ÚNICO para todos los ViewModels. No mezclar variantes.

```kotlin
// Estado observable
private val _uiState = MutableStateFlow(MyUiState())
val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

// Eventos one-shot (navegación, snackbars) — NO en el StateFlow
private val _events = Channel<MyUiEvent>(Channel.BUFFERED)
val events = _events.receiveAsFlow()
```

- Exponer con `asStateFlow()`. **NO** envolver un `MutableStateFlow` en `stateIn(...)` — genera dos
  canales de estado redundantes.
- En Screens: colectar con `collectAsStateWithLifecycle()`; eventos vía `LaunchedEffect` sobre `events`.
- Conectividad: ViewModels con red exponen `isOffline`/`isConnected` y la UI deshabilita acciones
  destructivas/submit cuando no hay red (`NetworkMonitor`).

---

## Error Handling Pattern

Se usa el `kotlin.Result<T>` estándar (NO un sealed propio). Convención de capas:

```kotlin
// Repo: operaciones que pueden fallar devuelven Result<T>
override suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> =
    runCatching { /* ... */ }

// ViewModel: mapea a UI state, nunca traga el error
viewModelScope.launch {
    repo.signInWithEmail(email, pass)
        .onSuccess { _uiState.update { s -> s.copy(isLoading = false) } }
        .onFailure { t ->
            Timber.e(t)
            FirebaseCrashlytics.getInstance().recordException(t)
            _uiState.update { s -> s.copy(isLoading = false, error = t.message) }
        }
}
```

- **Prohibido** `runCatching { ... }` sin `.onFailure { }` (traga el error en silencio).
- Errores de escritura remota fire-and-forget que el usuario no puede reintentar: igual loggear como
  non-fatal a Crashlytics, y donde aplique exponer feedback ("no se pudo sincronizar tu perfil").

---

## Data & Sync

Reglas de la sincronización Room ↔ Firestore. Tenerlas en cuenta ante cualquier cambio en el diario.

- **Room es la fuente de verdad.** Firestore es el espejo. La UI lee siempre de Room (Flows).
- **Idempotencia:** las escrituras remotas usan `.document(entity.id).set(...)` con ID determinístico
  → reescribir es seguro, no genera duplicados.
- **Flag `synced_at`:** `NULL` = pendiente de subir. Crear/editar setea `synced_at = NULL` y dispara
  `scheduleImmediateSync`. El worker sube y marca `synced_at`.
- **Borrados = soft-delete con tombstone.** Borrar NO hace hard-delete inmediato: marca `is_deleted = 1`,
  `synced_at = NULL` y dispara sync. El worker borra el doc remoto y recién ahí hace hard-delete local.
  Las queries de lectura filtran `is_deleted = 0`. El pull no resucita tombstones pendientes.
- **Pull additivo con guarda:** `pullRemoteEntries` solo inserta entradas remotas que no están
  localmente Y que no tienen tombstone local pendiente.

### Disciplina de migraciones Room (OBLIGATORIA)

1. Todo cambio de esquema (columna, tabla, índice, tipo) **bumpea `version`** en `CaloriesTrackerDatabase`.
2. Se escribe la `Migration(old, new)` correspondiente en `data/local/migration/Migrations.kt` y se
   registra con `.addMigrations(...)` en `DatabaseModule`.
3. Se agrega un test de migración con `MigrationTestHelper` (room-testing) usando el esquema exportado
   en `app/schemas/`.
4. **NUNCA** `fallbackToDestructiveMigration()` en builds de release — borra los datos del usuario.

---

## Code Quality

### Umbrales de tamaño — señales de review, NO reglas mecánicas

| Unidad | Umbral de alarma | Acción al cruzarlo |
|---|---|---|
| Función / método | ~50 líneas | Extraer funciones privadas que documenten los pasos |
| Composable (componente) | ~100 líneas | Descomponer en sub-composables |
| Composable (screen) | ~250 líneas | Extraer secciones a componentes |
| ViewModel / clase | ~300 líneas | Revisar responsabilidades — ¿hay más de un mundo adentro? |
| Archivo | ~400 líneas | Probablemente vive más de una cosa ahí — separar |

Cruzar un umbral dispara un **review**, no un split automático. El criterio es **cohesión**.

### Componentización
- Extraer un átomo reusable en la **tercera repetición** (regla de tres). Abstraer en el primer uso
  es over-engineering; copy-paste eterno es deuda.
- Los Composables reciben **estado + lambdas**, nunca referencias a ViewModel.

### Compose Performance
- `remember` para cómputos caros; `derivedStateOf` para valores derivados de estado cambiante.
- `LazyColumn`/`LazyRow`/`LazyVerticalGrid`: SIEMPRE `key`; `contentType` en listas heterogéneas.
- Nada de trabajo pesado en composición — efectos en `LaunchedEffect`, cómputo en el ViewModel/domain.

### Previews — ACTIVAS, nunca comentadas
Cada componente general y cada screen lleva un `@Preview` `private` activo al final del archivo, con
fake data, envuelto en el theme. R8 lo elimina del release. Un preview comentado se pudre en silencio.

---

## Testing

Frameworks: **JUnit4 + MockK + Turbine + kotlinx-coroutines-test** (unit, `app/src/test/`) y
**room-testing `MigrationTestHelper`** (instrumented, `app/src/androidTest/`).

Qué se testea con prioridad (donde el riesgo es real):
- Lógica de dominio extraída (parsing numérico, escalado de servings, formateo).
- **Migraciones Room** — cada `Migration` tiene su test; es lo que evita crashes en upgrades.
- Consistencia de sync (soft-delete no resucita; pendientes se reintentan).
- Mapeo de errores en repos (un fallo se loggea y se propaga como `Result.failure`).

No dejar tests placeholder (`ExampleUnitTest`/`ExampleInstrumentedTest`).

---

## Build & Release

- ProGuard + resource shrinking habilitados en release (`isMinifyEnabled`, `isShrinkResources`).
- Las reglas de kotlinx.serialization en `proguard-rules.pro` cubren los DTOs vía wildcard
  `com.juanpcf.caloriestracker.**$$serializer` — NO hace falta keep manual por DTO si están bajo ese package.
- `BuildConfig.OPENROUTER_API_KEY` / `USDA_API_KEY` vienen de `local.properties`. Nunca commitear keys.
- Esquemas Room versionados en `app/schemas/` (committeables, base para migration tests).
- Conventional commits, sin atribución de IA en los mensajes.

---

## Key Files Reference

| Archivo | Propósito |
|---|---|
| `CaloriesTrackerApp.kt` | Hilt init, WorkManager `Configuration.Provider`, Timber/Crashlytics |
| `MainActivity.kt` | Entry point, start destination, splash |
| `app/build.gradle.kts` | Build config, ProGuard, `BuildConfig` keys, Room schema dir |
| `gradle/libs.versions.toml` | Catálogo de versiones de dependencias |
| `core/di/` | Módulos Hilt (`DatabaseModule`, `FirebaseModule`, …) |
| `core/navigation/` | `Routes.kt` (rutas `@Serializable`), `NavGraph.kt`, `BottomNavBar.kt` |
| `data/sync/FirestoreSyncWorker.kt` | Sincronización Room ↔ Firestore |
| `data/firebase/` | Repos Firestore + `FirebaseAuthRepositoryImpl` |
| `data/local/` | Room: `CaloriesTrackerDatabase`, DAOs, entities, migrations |
| `data/remote/openrouter/` | API de IA + DTOs (kotlinx.serialization) |
| `proguard-rules.pro` | Reglas R8 para release |
| `app/schemas/` | Esquemas Room exportados (migraciones) |
