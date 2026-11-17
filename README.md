# mc-plugins
A collection of Minecraft server plugins for pupur, built against the [Purpur](https://purpurmc.org) API.

## Requirements
- **JDK 25** (Minecraft 26.x / Purpur 26.3 requires Java 25)
- Nothing else — the Gradle wrapper (`./gradlew`) downloads Gradle for you.

## Build
```sh
./gradlew build
```
Each plugin's jar ends up in `plugins/<name>/build/libs/`. Drop it into your server's `plugins/` folder.

Build just one plugin: `./gradlew :example:build`

## Layout
```
plugins/
  example/            # one folder per plugin
    build.gradle.kts  # just: plugins { id("mc-plugin") }
    src/main/java/...
    src/main/resources/plugin.yml
buildSrc/             # shared build config (Java version, Purpur API, repos)
gradle.properties     # Purpur API version + plugin version
```

## Adding a new plugin
1. Copy `plugins/example` to `plugins/<your-plugin>`.
2. Rename the Java package/class and update `name`, `main`, and commands in `plugin.yml`.
3. Run `./gradlew build` — new folders are picked up automatically.

## Updating Minecraft/Purpur
Bump `purpurVersion` in `gradle.properties` (and `api-version` in each `plugin.yml` for a new major version).
Available versions: https://repo.purpurmc.org/snapshots/org/purpurmc/purpur/purpur-api/

## CI
GitHub Actions builds every push and PR to `main` and uploads the plugin jars as a build artifact.
