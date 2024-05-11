plugins {
    id("xyz.jpenilla.run-paper").version("2.3.0")
    id("io.github.goooler.shadow").version("8.1.7")
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
        val apiVersion = gameVersion.split(".").take(2).joinToString(".")
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
