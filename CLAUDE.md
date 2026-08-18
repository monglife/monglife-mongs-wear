# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mongs is a Wear OS + Android app for a "growing a pet with your step count" service (다마고치-style). This
repo (`monglife-mongs-wear`) contains two installable apps that share almost all business logic:

- `app/wear-app` — the original Wear OS app (published on Play Store).
- `app/mobile-app` — a companion phone app added more recently (see `feat: 모바일 앱 개발 초기 설정`).

Backend lives in a separate repo: https://github.com/MongLife/monglife-mongs

## Build & Common Commands

Standard Gradle wrapper. Run from the repo root.

Every Android module has a `profile` flavor dimension with three flavors — `dev`, `stg`, `prd` — so
build tasks carry the environment in their name (`assembleDevDebug`, not `assembleDebug`).

```bash
./gradlew :app:wear-app:assembleDevDebug      # build the wear app against dev
./gradlew :app:mobile-app:assembleDevDebug    # build the mobile app against dev
./gradlew :app:wear-app:installDevDebug       # install wear app on connected device/emulator
./gradlew :app:wear-app:assemblePrdRelease    # signed, R8-minified production build
./gradlew testDevDebugUnitTest                # run all unit tests (plain `test` runs all 3 flavors)
./gradlew :application:mong-application:test  # run tests for one module (pure JVM, no flavors)
./gradlew :application:mong-application:test --tests "*.GetCurrentMongUseCaseTest"  # single test
./gradlew lintDevDebug                        # Android lint across modules
```

`assembleDebug` still exists but builds all three environments at once — prefer the explicit
`assemble<Flavor>Debug` form. In Android Studio, switch environments with the Build Variants panel.

There is no CI config in this repo (no `.github/workflows`) and no top-level lint/format script beyond
Gradle's built-in `lint` task.

### Environment/config files (`configs` submodule)

`.gitmodules` points `configs/` at a private submodule (`monglife-mongs-wear-sub`). **Nothing is copied
out of it** — the build reads it in place, so `git submodule update --init` is the only setup step.

```
configs/core/data-core/{dev,stg,prd}/values/config.xml   per-environment string resources (13 keys)
configs/app/<module>/res/values/strings.xml              app_name (environment-independent)
configs/app/<module>/keystore.properties                 release signing
configs/app/<module>/google-services.json                FCM (environment-independent)
configs/keyStore/monglife                                the keystore itself
```

How each one is wired:

- **Environment strings** — `core/data-core/build.gradle` points each flavor's `res.srcDir` at
  `configs/core/data-core/<flavor>`. This is the *only* place environments differ; everything else in
  `configs/` is shared. The three `config.xml` files must declare an identical key set, since the `R`
  class is generated per variant and a key missing from one flavor is a compile error in that flavor.
- **Flavor declaration** — the root `build.gradle` declares the dimension for *every* Android module via
  `plugins.withId("com.android.base")`. It must be every module, not just `core:data-core`: a module that
  depends on a flavored one without declaring the dimension fails variant resolution.
- **Signing / app_name** — the two `app/*/build.gradle` files reference `configs/app/<module>/` directly.
- **`google-services.json`** — the plugin ignores source sets and offers no path setting, but
  `GoogleServicesTask.googleServicesJsonFiles` is a public input property, so the app modules override it
  inside `afterEvaluate` (earlier than that and the plugin's own default overwrites it). Re-verify this
  wiring when bumping `gmsPluginVersion`.
- **`applicationIdSuffix` must stay off.** Each `google-services.json` holds a single client
  (`com.mongs.wear` / `com.mongs.mobile`) matched exactly by package name; a suffix fails the build.
  Use `versionNameSuffix` if you need to tell builds apart.
- **Cleartext HTTP** — `app/*/src/{dev,stg}/res/xml/network_security_config.xml` permit it, `src/main`
  blocks it. Split by flavor rather than build type, because the environment is what decides it.

## Architecture

This is a multi-module Gradle project following **Clean/Hexagonal Architecture**, layered top-to-bottom as
`app → presentation → application → domain`, with `data` implementing the ports that `application` declares.
Each business area ("vertical slice") has its own module in every layer: `auth`, `battle`, `device`,
`member`, `mong`.

```
app/wear-app, app/mobile-app          (Android apps, Hilt entry points)
        │
presentation/wear-view-presentation   (Compose UI, wear-specific widgets)
presentation/mobile-view-presentation (Compose UI, mobile-specific widgets)
        │  both depend on:
presentation/viewmodel-presentation   (ViewModels shared by both apps — one module, no per-app duplication)
        │
application/{auth,battle,device,member,mong}-application   (use cases, ports, VOs — pure Kotlin/JVM)
        │
domain/{auth,battle,device,member,mong}-domain             (entities, enums — pure Kotlin/JVM, zero deps)

data/{auth,battle,device,member,mong}-data   (Android libs: implements application's ports via
                                               Retrofit/Room/MQTT/DataStore adapters, wired with Hilt)

core/common-core        pure Kotlin: ErrorCode/ErrorException, Base*UseCase "skell" templates
core/application-core   pure Kotlin: BaseParamUseCase / BaseNoParamUseCase, Page/PageResponse
core/presentation-core  Android lib: BaseViewModel (shared exception handling + toast events)
core/data-core          Android lib: shared Retrofit/OkHttp, DataStore, MQTT client setup
core/billing-core       Android lib: Google Play Billing wrapper
```

Key wiring facts (see `settings.gradle` and per-module `build.gradle` for the full graph):

- `domain/*` modules have **no dependencies at all** — just models/enums.
- `application/*` modules depend only on `core:application-core` (`api`) — no Android APIs, testable as
  plain JUnit.
- `data/*` modules depend on the matching `application/*` module plus `core:data-core`; they are Android
  library modules (Room, Retrofit, MQTT, Hilt `@Binds`/`@Provides` live here).
- `presentation:viewmodel-presentation` is the one place that wires everything together: it `api`-exposes
  every `application/*` module (so ViewModels can call use cases) and `implementation`-depends on every
  `data/*` module (so Hilt has adapters to bind at runtime), plus `core:billing-core`.
- `app/wear-app` and `app/mobile-app` only depend on `core:data-core` + their respective
  `presentation:*-view-presentation` module — they contain almost no business logic themselves (just
  `MainActivity`, `MainApplication`, notification service/module).

### Per-slice package pattern (example: `mong`)

For a given vertical slice, follow this pattern when adding a feature — it is consistent across `auth`,
`battle`, `device`, `member`, `mong`:

- `domain/mong-domain/.../model/` — plain domain models (e.g. `Mong.kt`, `Food.kt`) and `enums/`.
- `application/mong-application/.../port/{persistence,web}/` — interfaces the use cases depend on
  (`ManagementPersistencePort`, `ManagementWebPort`, etc.), plus `port/web/response/` DTOs for expected
  responses.
- `application/mong-application/.../usecase/<feature>/` — one class per use case, extending
  `BaseParamUseCase<Command, Result>` or `BaseNoParamUseCase<Result>` from `core:application-core`, injected
  via `@Inject constructor` with the ports it needs. Executed through `invoke()`.
- `application/mong-application/.../vo/` — view objects (`MongVo`, etc.) returned by use cases to the
  presentation layer, built via a `.of(...)` factory from the domain model.
- `application/mong-application/.../error/` + `exception/` — module-specific `ErrorCode`/`Exception`
  subclasses.
- `data/mong-data/.../persistence/adapter/` and `.../web/adapter/` — `@Singleton class XAdapter @Inject
  constructor(...) : XPort` implementations using Room DAOs (`persistence/dao`, `persistence/entity`,
  `persistence/db`) and Retrofit clients (`web/client`, `web/client/request`, `web/client/response`).
- `data/mong-data/.../Module.kt` — Hilt `@Module` binding each adapter to its port (`AdapterModule`) and
  providing Retrofit-backed web clients (`WebClientModule`).
- `presentation/viewmodel-presentation/.../pages/<feature>/` — `XViewModel : BaseViewModel()` classes that
  call use cases and expose `StateFlow`/VO state to Compose screens.

`core/common-core/.../skells/BaseParamUseCaseSkell.kt` and `ViewModelSkell.kt` are (commented-out) copy-paste
templates for scaffolding a new use case / view model in this style — check them when adding a new one.

### Base classes worth knowing

- `core/application-core` `BaseParamUseCase<P, R>` / `BaseNoParamUseCase<R>` — all use cases extend one of
  these; call sites use `useCase(command)` (operator `invoke`), not `.execute()` directly.
- `core/presentation-core` `BaseViewModel` — provides `viewModelScopeWithHandler` (coroutine scope with a
  shared `CoroutineExceptionHandler` that logs, and if the thrown exception is an `ErrorException` whose
  `ErrorCode.isMessageShow()` is true, emits it on the shared `errorEvent` toast flow after
  `NAVIGATE_DELAY`), plus `observeForever`/`observeStop` helpers for subscribing a `Flow` into a
  `MutableStateFlow` for the life of the ViewModel.
- Real-time updates use MQTT (`core:data-core`'s `MqttClient`, subscribed to per-entity topics like
  `mong/management/{mongId}`) alongside Room as the local cache — see
  `ManagementPersistenceAdapter.getMongFlow` for the subscribe/shareIn/unsubscribe pattern used whenever a
  persistence adapter needs to reflect server push updates into a local `Flow`.

### Notifications / FCM

Both apps have their own `NotificationModule.kt` / `NotificationService.kt` under
`app/{wear,mobile}-app/.../module` and `.../service` — these are per-app (not shared through
`core:data-core`), since FCM handling differs between the watch and phone.

### Kotlin/Android versions

Centralized in root `build.gradle` (`ext` block): Kotlin 2.4.10, AGP 9.3.1, Hilt 2.60.1, Java 17,
`compileSdk 36`, `minSdk 30`. Per-module `buildscript.ext` blocks add module-specific library versions
(Compose, Retrofit, Room, etc.) — check the relevant module's `build.gradle` before assuming a shared
version applies.
