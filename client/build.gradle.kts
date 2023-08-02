import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "5.0.0"
}

node {
    download.set(true)
}

tasks {
    register<NpmTask>("generate") {
        dependsOn("npmInstall")
        args.set(listOf("run", "generate:with-api"))
    }
}
