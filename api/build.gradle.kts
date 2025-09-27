plugins {
    id("java")
}

group = "dev.ocean.api"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT")

}

tasks.test {
    useJUnitPlatform()
}