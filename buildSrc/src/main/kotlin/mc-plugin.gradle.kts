// Shared setup applied by every plugin project via `plugins { id("mc-plugin") }`.
plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
}

val purpurVersion = providers.gradleProperty("purpurVersion").get()

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:$purpurVersion")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

// Fills ${version} etc. into plugin.yml so the jar version is set in one place.
tasks.processResources {
    val props = mapOf("name" to project.name, "version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(props) }
}

tasks.jar {
    archiveBaseName = project.name
}
