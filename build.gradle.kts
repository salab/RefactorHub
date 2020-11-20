plugins {
    application
    kotlin("jvm") version "1.3.70"
    kotlin("plugin.serialization") version "1.3.70"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "9.2.1"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("org.flywaydb.flyway") version "6.5.1"
}

group = "jp.ac.titech.cs.se"
version = "0.0.1-SNAPSHOT"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/exposed") }
}

val ktor_version: String by project
val koin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val h2_version: String by project
val hikaricp_version: String by project
val postgresql_version: String by project
val refminer_version: String by project

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")

    // DI
    implementation("org.koin:koin-ktor:$koin_version")

    // DB
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.zaxxer:HikariCP:$hikaricp_version")
    implementation("org.postgresql:postgresql:$postgresql_version")
    runtimeOnly("com.h2database:h2:$h2_version")

    // Log
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.codehaus.janino:janino:3.+")

    // Tools
    implementation("org.eclipse.jdt:org.eclipse.jdt.core:3.+")
    implementation("com.github.tsantalis:refactoring-miner:$refminer_version") {
        exclude("org.slf4j")
    }
    // implementation("com.github.tsantalis:RefactoringMiner:master-SNAPSHOT")

    // Other
    implementation("org.kohsuke:github-api:1.+")
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.+")

    // Test
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

ktlint {
    // See https://github.com/pinterest/ktlint/issues/527
    disabledRules.set(setOf("import-ordering"))
}

flyway {
    url = System.getenv("DATABASE_JDBC_URL")
    user = System.getenv("DATABASE_USERNAME")
    password = System.getenv("DATABASE_PASSWORD")
    baselineOnMigrate = true
}

tasks {
    withType<Jar> {
        dependsOn("processClient")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to application.mainClassName
                )
            )
        }
    }
    register<Copy>("processClient") {
        dependsOn(":client:build")
        from("client/dist") {
            include("**/*.*")
        }
        into("${project.buildDir}/resources/main/static")
    }
}
