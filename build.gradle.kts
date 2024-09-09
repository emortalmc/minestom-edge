plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.emortal.minestom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://packages.confluent.io/maven/")
}

dependencies {
    // Minestom
    implementation("net.minestom:minestom-snapshots:7ce047b22e")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    // Logger
    implementation("ch.qos.logback:logback-classic:1.5.1")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // EmortalMC
    implementation("dev.emortal.api:common-proto-sdk:b05808d")
    implementation("dev.emortal.api:agones-sdk:1.1.0")

    // Monitoring
    implementation("io.pyroscope:agent:0.12.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.1")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-gson:0.12.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    shadowJar {
        mergeServiceFiles()

        manifest {
            attributes(
                "Main-Class" to "dev.emortal.minestom.edge.Entrypoint",
                "Multi-Release" to true
            )
        }
    }

    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    build {
        dependsOn(shadowJar)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}