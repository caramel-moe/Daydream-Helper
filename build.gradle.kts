/* Imports */
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import java.util.Locale

/* Plugins */
plugins {
    id("java")
    id("maven-publish")
    id("io.papermc.paperweight.userdev").version("2.0.0-beta.18")
}

/* Project Info */
allprojects {
    group = "moe.caramel"
    version = property("version") as String
    description = "Wrapper for plugins that cannot use Daydream dependencies"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

/* Dependencies */
val gameVersion = property("version") as String

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven(url = "https://repo.caramel.moe/repository/maven-public/") // caramel-repo
        maven(url = "https://repo.papermc.io/repository/maven-public/") // papermc-repo
    }

    dependencies {
        /* Daydream API */
        compileOnly("moe.caramel", "daydream-api", gameVersion)
    }
}

dependencies {
    /* Paper Server */
    paperweight.paperDevBundle(gameVersion) {
        exclude(module = "paper-mojangapi")
        exclude(module = "paper-api")
    }
}

configurations.all { // OMG
    exclude("io.papermc.paper", "paper-api")
}

/* Tasks */
tasks {
    build {
        if (rootProject.childProjects["test-plugin"] != null) {
            finalizedBy(rootProject.project(":test-plugin").tasks.build)
        }
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

/* Publish */
val isSnapshot = project.version.toString().endsWith("-SNAPSHOT")
configure<PublishingExtension> {
    repositories.maven {
        name = "caramel"
        url = uri("https://repo.caramel.moe/repository/maven-" + (if (isSnapshot) "snapshots" else "releases") + "/")
        credentials {
            username = System.getenv("DEPLOY_ID")
            password = System.getenv("DEPLOY_PW")
        }
    }

    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase(Locale.ENGLISH)
        from(components["java"])
    }
}
