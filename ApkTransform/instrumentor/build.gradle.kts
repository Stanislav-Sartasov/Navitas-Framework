import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    groovy
    id("org.jetbrains.intellij") version "0.4.9"
    java
    kotlin("jvm") version "1.3.41"
    `java-gradle-plugin`
    maven
}

group = "com.lanit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    compile("org.codehaus.groovy:groovy-all:2.3.11")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.android.tools.build:gradle:3.2.1")
    implementation("com.android.tools.build:gradle-api:3.2.1")
    implementation("org.javassist:javassist:3.23.1-GA")
    testCompile("junit", "junit", "4.12")
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

gradlePlugin {
    plugins {
        create("instrumentor") {
            id = "com.lanit.instrumentor"
            implementationClass = "test.InstrPlugin"
        }
    }
}

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
}