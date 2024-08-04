plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://artifacts.glencoesoftware.com/artifactory/unidata-releases")
    maven("https://artifacts.openmicroscopy.org/artifactory/maven/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(group = "ome", name = "formats-gpl", version = "7.3.0")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.3.14")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
}

tasks.test {
    useJUnitPlatform()
}