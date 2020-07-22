import com.moowork.gradle.node.yarn.YarnTask

plugins {
    id("com.github.node-gradle.node") version "2.2.4"
}

node {
    download = true
}

tasks {
    register<YarnTask>("build") {
        dependsOn("yarn")
        args = listOf("build")
    }
}
