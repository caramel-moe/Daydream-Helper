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
}

dependencies {
    /* Bukkit API */
    compileOnly("moe.caramel", "daydream-api", property("ver_bukkit") as String)
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }
}

/* Publish */
configure<PublishingExtension> {
    repositories.maven {
        name = "caramel"
        url = uri("https://repo.caramel.moe/repository/maven-public/")
        credentials(PasswordCredentials::class)
    }

    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase(Locale.ENGLISH)
        from(components["java"])
    }
}
