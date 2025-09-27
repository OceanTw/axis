plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "dev.ocean.arc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.panda-lang.org/releases")
    maven("https://jitpack.io")

    maven("https://maven.enginehub.org/repo/")
//    maven("https://repo.codemc.io/repository/maven-releases/")
//    maven("https://repo.codemc.io/repository/maven-snapshots/")
//    maven("https://maven.evokegames.gg/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT")
    implementation("com.github.Devlrxxh:BlockChanger:23397d3")
    implementation("org.reflections:reflections:0.10.2")
    implementation("dev.rollczi:litecommands-bukkit:3.10.4")
//    compileOnly("com.github.retrooper:packetevents-spigot:2.9.5")
//    implementation("me.tofaa.entitylib:spigot:+74871b3-SNAPSHOT")
    implementation("com.github.luben:zstd-jni:1.5.7-4")

    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    implementation("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation(platform("com.intellectualsites.bom:bom-newest:1.55")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
}