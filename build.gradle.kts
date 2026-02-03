import org.gradle.jvm.tasks.Jar

plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

group = "com.chronoshift"
version = "1.0.0"

repositories {
    maven("https://www.cursemaven.com")
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly("curse.maven:hyui-1431415:7540267")
    compileOnly("org.ow2.asm:asm:9.6")
    compileOnly("org.ow2.asm:asm-commons:9.6")

    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Disable default jar
tasks.jar { enabled = false }

// ---- Chronoshift jar ----
val jarChronoshift = tasks.register<Jar>("jarChronoshift") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("Chronoshift")
    archiveVersion.set(project.version.toString())

    from(sourceSets.main.get().output.classesDirs) {
        include("com/chronoshift/**")
    }

    // include Gson for JSON
    from({
        configurations.runtimeClasspath.get().filter { it.name.contains("gson") }
            .map { if (it.isDirectory) it else zipTree(it) }
    }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    // Chronoshift manifest at jar root
    from("src/main/resources/chronoshift") {
        include("manifest.json")
        into("")
    }

    // Include assets in the JAR (Server and Common folders)
    from("src/main/resources/chronoshift/Server") {
        into("Server")
    }
    from("src/main/resources/chronoshift/Common") {
        into("Common")
    }
}

// Build Chronoshift jar on build
tasks.build {
    dependsOn(jarChronoshift)
}
