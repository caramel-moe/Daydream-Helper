/* Imports */
import java.util.Locale

/* Plugins */
plugins {
    id("java")
    id("maven-publish")
    id("io.papermc.paperweight.userdev").version("1.5.15")
}

/* Project Info */
allprojects {
    group = "moe.caramel"
    version = property("version") as String
    description = "Wrapper for plugins that cannot use Daydream dependencies"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))

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

    assemble { dependsOn(reobfJar) }
    reobfJar { outputJar.set(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar")) }
    withType<PublishToMavenRepository> {
        dependsOn(assemble)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

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
        artifact("build/libs/${project.name}-${project.version}.jar") {
            classifier = null
        }
        artifact("build/libs/${project.name}-${project.version}-javadoc.jar") {
            classifier = "javadoc"
        }
        artifact("build/libs/${project.name}-${project.version}-sources.jar") {
            classifier = "sources"
        }
    }
}
