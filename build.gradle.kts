import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    kotlin("jvm") version "1.6.20"
    `maven-publish`
    signing
}

val publishingProperties = loadProperties("publishing.properties")

group = "co.appreactor"
version = "0.2.3"

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("feedk")
                description.set("Web feed parser for Kotlin")
                url.set("https://github.com/bubelov/feedk")

                licenses {
                    license {
                        name.set("AGPL-3.0 License")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.en.html")
                    }
                }

                developers {
                    developer {
                        id.set("bubelov")
                        name.set("Igor Bubelov")
                        email.set("igor@bubelov.com")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:bubelov/feedk.git")
                    developerConnection.set("scm:git:git@github.com:bubelov/feedk.git")
                    url.set("https://github.com/bubelov/feedk")
                }
            }
        }
    }

    repositories {
        maven {
            name = "nexus"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = publishingProperties.getProperty("nexus.username")
                password = publishingProperties.getProperty("nexus.password")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        publishingProperties.getProperty("signing.key"),
        publishingProperties.getProperty("signing.password")
    )
    sign(publishing.publications["mavenJava"])
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

gradle.projectsEvaluated {
    tasks.withType(Test::class.java).forEach {
        it.addTestListener(object : TestListener {
            override fun beforeSuite(desc: TestDescriptor) {

            }

            override fun afterSuite(desc: TestDescriptor, result: TestResult) {

            }

            override fun beforeTest(desc: TestDescriptor) {

            }

            override fun afterTest(desc: TestDescriptor, result: TestResult) {
                println("test ${desc.name} ... ${result.resultType}")
            }
        })
    }
}