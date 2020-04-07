import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.publish.PublishingExtension

plugins {
    //groovy
    //java
    id("org.jetbrains.intellij") version "0.4.9"
    kotlin("jvm") version "1.3.41"
    `java-gradle-plugin`
    //maven
    `maven-publish`
}

group = "com.Navitas"
version = "1.11"

repositories {
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.android.tools.build:gradle:3.2.1")
    implementation("com.android.tools.build:gradle-api:3.2.1")
    implementation("org.javassist:javassist:3.23.1-GA")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2019.1.3"
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      Add change notes here.<br>
      <em>most HTML tags may be used</em>""")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

configure<PublishingExtension> {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/stanislav-sartasov/navitas-framework")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "NaviProf"
            from(components["java"])
            
            pom {
                name.set("NaviProf")
                description.set("A profiling tool for power estimation")
                url.set("http://www.example.com/library") //TODO

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Shaposhnikov-Alexey")
                        name.set("Shaposhnikov Alexey")
                        email.set("shaposhnikov.lyosha@gmail.com")
                    }
                    developer {
                        id.set("johnd")
                        name.set("John Doe")
                        email.set("john.doe@example.com")
                    } //TODO: write other developers
                }
            }
        }
    }
}

/*
tasks{
    "uploadArchives"(Upload::class) {
        repositories {
            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    withGroovyBuilder {
                        "repository"("url" to uri("repo"))
                    }
                }
            }
        }
    }
}*/