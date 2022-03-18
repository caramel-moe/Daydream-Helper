/* Imports */
import java.util.Locale

plugins {
    id("java")
    id("maven-publish")
}

allprojects {
    group = "moe.caramel"
    version = property("projectVersion") as String
    description = "Wrapper for plugins that cannot use Daydream dependencies"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

dependencies {
    /* Daydream API */
    compileOnly("moe.caramel", "daydream-api", property("ver_bukkit") as String)
}

/* Tasks */
tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<ProcessResources> {
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
        artifactId = project.name.toLowerCase(Locale.ENGLISH)
        from(components["java"])
    }
}
