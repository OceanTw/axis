plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0"
}

group = "dev.ocean"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}