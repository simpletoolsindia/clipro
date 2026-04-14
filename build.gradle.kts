plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    // JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    // Git
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.6.0.202603022253-r")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.12")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Create a runnable JAR
tasks.register<Jar>("uberJar") {
    archiveBaseName.set("clipro")
    archiveVersion.set("0.1.0")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    manifest {
        attributes["Main-Class"] = "com.clipro.App"
        attributes["Implementation-Version"] = project.version
    }
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

// GraalVM Native Image configuration
tasks.register("nativeCompile") {
    group = "build"
    description = "Compile native image (requires GraalVM)"
    doLast {
        println("GraalVM native image compilation requires GraalVM SDK to be installed.")
        println("Set GRAALVM_HOME environment variable and run 'native-image' command.")
        println("")
        println("Expected command after GraalVM install:")
        println("  native-image -jar build/libs/clipro-0.1.0-uber.jar")
    }
}
