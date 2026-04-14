plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

val tamboVersion = "0.2.0-SNAPSHOT"

dependencies {
    implementation("dev.tamboui:tamboui-core:$tamboVersion")
    implementation("dev.tamboui:tamboui-toolkit:$tamboVersion")
    implementation("dev.tamboui:tamboui-widgets:$tamboVersion")
    implementation("dev.tamboui:tamboui-tui:$tamboVersion")
    implementation("dev.tamboui:tamboui-jline3-backend:$tamboVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.6.0.202603022253-r")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.openjfx:javafx-controls:17.0.12")
    implementation("org.openjfx:javafx-graphics:17.0.12")
    implementation("org.openjfx:javafx-base:17.0.12")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.12")
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

// Build uberJar
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
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

// Fix uberJar by removing signature files
tasks.register<Exec>("fixJar") {
    dependsOn("uberJar")
    workingDir = projectDir
    commandLine = listOf("/bin/bash", "-c", """
        cd build/libs && \
        rm -rf /tmp/clipro-fix && \
        mkdir -p /tmp/clipro-fix && \
        cd /tmp/clipro-fix && \
        unzip -q /Users/sridhar/clipro/build/libs/clipro-0.1.0.jar && \
        find . -name "*.SF" -delete && \
        find . -name "*.RSA" -delete && \
        find . -name "*.DSA" -delete && \
        rm -f /Users/sridhar/clipro/build/libs/clipro-0.1.0.jar && \
        jar cfm /Users/sridhar/clipro/build/libs/clipro-0.1.0.jar META-INF/MANIFEST.MF . && \
        rm -rf /tmp/clipro-fix && \
        echo "Fixed: signature files removed"
    """.trimIndent())
}

// Default build
tasks.build {
    dependsOn("fixJar")
}
