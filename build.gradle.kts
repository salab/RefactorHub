plugins {
    application
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlint
    id("org.jlleitschuh.gradle.ktlint-idea") version Versions.ktlint
    id("com.github.johnrengelman.shadow") version Versions.shadow
}

group = "jp.ac.titech.cs.se"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")

    // Ktor
    implementation("io.ktor:ktor-server-core:${Versions.ktor}")
    implementation("io.ktor:ktor-server-netty:${Versions.ktor}")
    implementation("io.ktor:ktor-server-host-common:${Versions.ktor}")
    implementation("io.ktor:ktor-locations:${Versions.ktor}")
    implementation("io.ktor:ktor-client-apache:${Versions.ktor}")
    implementation("io.ktor:ktor-auth:${Versions.ktor}")
    implementation("io.ktor:ktor-jackson:${Versions.ktor}")

    // DI
    implementation("org.koin:koin-ktor:${Versions.koin}")

    // DB
    implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed}")
    implementation("org.jetbrains.exposed:exposed-dao:${Versions.exposed}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}")
    implementation("com.zaxxer:HikariCP:${Versions.hikaricp}")
    implementation("org.flywaydb:flyway-core:${Versions.flyway}")
    implementation("org.postgresql:postgresql:${Versions.postgresql}")
    runtimeOnly("com.h2database:h2:${Versions.h2}")

    // Log
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("org.codehaus.janino:janino:${Versions.janino}")

    // Tools
    implementation("org.eclipse.jdt:org.eclipse.jdt.core:${Versions.jdt}")
    implementation("com.github.tsantalis:refactoring-miner:${Versions.refminer}") {
        exclude("org.slf4j")
    }

    // Other
    implementation("org.kohsuke:github-api:${Versions.github}")
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

    // Test
    testImplementation(kotlin("test-junit"))
    testImplementation("io.ktor:ktor-server-tests:${Versions.ktor}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
}

tasks {
    withType<Jar> {
        dependsOn("processClient")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to application.mainClass.get()
                )
            )
        }
    }
    register<Copy>("processClient") {
        dependsOn(":client:generate")
        from("client/dist") {
            include("**/*.*")
        }
        into("${project.buildDir}/resources/main/static")
    }
}
