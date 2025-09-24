plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0"
}

group = "dev.ocean.axis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.panda-lang.org/releases")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://maven.evokegames.gg/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT")
    implementation("com.github.Devlrxxh:BlockChanger:23397d3")
    implementation("org.reflections:reflections:0.10.2")
    implementation("dev.rollczi:litecommands-bukkit:3.10.4")
    compileOnly("com.github.retrooper:packetevents-spigot:2.9.5")
    implementation("me.tofaa.entitylib:spigot:+74871b3-SNAPSHOT")

    implementation("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}