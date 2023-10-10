/* Imports */
import java.util.Locale

/* Plugins */
plugins {
    id("java")
    id("maven-publish")
    id("io.papermc.paperweight.userdev").version("1.5.8")
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
repositories {
    mavenCentral()
    maven(url = "https://repo.caramel.moe/repository/maven-public/") // caramel-repo
    maven(url = "https://papermc.io/repo/repository/maven-public/") // papermc-repo
}

dependencies {
    /* Daydream API */
    val version = property("version") as String
    compileOnly("moe.caramel", "daydream-api", version)

    /* Paper Server */
    paperweight.paperDevBundle(version) {
        exclude(module = "paper-mojangapi")
        exclude(module = "paper-api")
    }
}

configurations.all { // OMG
    exclude("io.papermc.paper", "paper-api")
}

/* Tasks */
tasks {
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
