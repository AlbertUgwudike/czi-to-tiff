plugins {
    // kotlin("jvm") version "1.9.23"
    kotlin("jvm") version "2.1.0"
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

val fatJar = task("fatJarr", type = Jar::class) {
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "MainKt"
    }
    from(configurations.compileClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}


//tasks.jar {
//    manifest {
//        attributes(hashMapOf("Main-Class" to "MainKt"))
//    }
//    from { configurations.runtimeClasspath.
//}