rootProject.name = "Daydream-Helper"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://repo.papermc.io/repository/maven-public/") // papermc-repo
    }
}

val testPlugin = file("test-plugin.settings.gradle.kts")
if (testPlugin.exists()) {
    apply(from = testPlugin)
} else {
    testPlugin.writeText("// Uncomment to enable the test plugin module\n//include(\":test-plugin\")\n")
}
