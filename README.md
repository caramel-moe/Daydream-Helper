<div align="center">

## Daydream-Helper

<p>Daydream-Helper는 Daydream Dependency를 사용할 수 없는 상황에서 매우 유용하며, 불안정하여 Daydream에 추가할 수 없는 유틸리티 메서드를 구현합니다.</p>

[![caramel.moe](https://img.shields.io/badge/made%20by.-caramel.moe-red)](https://caramel.moe)
[![Build Status](https://img.shields.io/github/actions/workflow/status/caramel-moe/Daydream-Helper/helper-build-and-publish.yml)](https://img.shields.io/github/actions/workflow/status/caramel-moe/Daydream-Helper/helper-build-and-publish.yml)
[![Discord](https://img.shields.io/discord/534586842079821824.svg?label=use%20server&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/f9qGtYF)
</div>

---

### Dependency 정보
Gradle
```kotlin
repositories {
    // caramel.moe Repository
    maven("https://repo.caramel.moe/repository/maven-public/")
}

dependencies {
    // Daydream Helper
    implementation("moe.caramel", "daydream-helper", "1.21.10-R0.1-SNAPSHOT")
}

// (1) Shadow를 사용하지 않는 경우
tasks.jar {
    manifest.attributes["paperweight-mappings-namespace"] = "mojang+yarn"
}

// (2) Shadow를 사용하는 경우
tasks.shadowJar {
    manifest.attributes["paperweight-mappings-namespace"] = "mojang+yarn"
}

// (3) paperweight를 사용하는 경우
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
```
