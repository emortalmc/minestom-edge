plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta11"
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
    implementation("net.minestom:minestom-snapshots:1_21_5-0473b41b2a")
    implementation("net.kyori:adventure-text-minimessage:4.18.0")

    // Logger
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // EmortalMC
    implementation("dev.emortal.api:common-proto-sdk:b05808d")
    implementation("dev.emortal.api:agones-sdk:8a0d297")

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