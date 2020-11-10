import com.moowork.gradle.node.npm.NpmTask

plugins {
    id("com.github.node-gradle.node") version "2.2.4"
}

node {
    download = true
}

tasks {
    register<NpmTask>("build") {
        dependsOn("npmInstall")
        setArgs(listOf("run", "generate:with-api"))
    }
}
