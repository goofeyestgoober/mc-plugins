rootProject.name = "mc-plugins"

// Every folder in plugins/ that has a build.gradle.kts is picked up automatically.
// To add a plugin: copy plugins/example, rename it, and change its package/plugin.yml.
file("plugins").listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").exists() }
    ?.sortedBy { it.name }
    ?.forEach { include(":${it.name}"); project(":${it.name}").projectDir = it }
