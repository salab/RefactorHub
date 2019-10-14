import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "1.3.50"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "2.1.9.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
}

group = "jp.ac.titech.cs.se"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    // Swagger
    implementation("io.springfox:springfox-swagger2:2.+")
    implementation("io.springfox:springfox-swagger-ui:2.+")
    // Parser
    implementation("org.eclipse.jdt:org.eclipse.jdt.core:3.+")
    // Other
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.kohsuke:github-api:1.+")
    runtimeOnly("mysql:mysql-connector-java:8.+")
    runtimeOnly("com.h2database:h2")
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
    withType<BootJar> {
        dependsOn("generateResources")
    }
    register<Copy>("generateResources") {
        dependsOn(":client:generate")
        from("client/dist") {
            include("**/*.*")
        }
        into("${project.buildDir}/resources/main/static")
    }
}
