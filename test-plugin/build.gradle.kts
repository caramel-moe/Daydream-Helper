plugins {
    id("com.gradleup.shadow").version("8.3.5")
    id("xyz.jpenilla.run-paper").version("2.3.1")
}

val gameVersion = rootProject.version.toString().split("-")[0]
version = gameVersion

dependencies {
    implementation(rootProject)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    jar {
        manifest.attributes["paperweight-mappings-namespace"] = "mojang+yarn"
    }

    runServer {
        minecraftVersion(gameVersion)
        runDirectory.set(layout.projectDirectory.dir("run").dir(gameVersion))
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val apiVersion = gameVersion.split("-")[0]
        val props = mapOf(
            "version" to project.version,
            "apiversion" to "\"$apiVersion\"",
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
