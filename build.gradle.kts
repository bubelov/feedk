import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    kotlin("jvm") version "1.5.20"
    `java-library`
    `maven-publish`
    signing
}

val publishingProperties = loadProperties("publishing.properties")

group = "co.appreactor"
version = "0.1"

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("feedk") {
            from(components["kotlin"])
        }
    }

    repositories {
        maven {
            credentials {
                username = publishingProperties.getProperty("nexus.username")
                password = publishingProperties.getProperty("nexus.password")
            }

            name = "feedk"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
}

signing {
    useInMemoryPgpKeys(
        publishingProperties.getProperty("signing.key"),
        publishingProperties.getProperty("signing.password")
    )
    sign(publishing.publications["feedk"])
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}
