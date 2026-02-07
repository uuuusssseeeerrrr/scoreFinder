import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.compose") version "1.10.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
    id("com.github.gmazzo.buildconfig") version "6.0.7"
}

group = "com.score"
version = "1.0.0"

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.14")
    implementation("ch.qos.logback:logback-classic:1.5.27")
    implementation("com.prof18.rssparser:rssparser:6.1.3")
    implementation("com.github.gmazzo.buildconfig:plugin:6.0.7")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.openjfx:javafx-base:21.0.10:win")
    implementation("org.openjfx:javafx-graphics:21.0.10:win")
    implementation("org.openjfx:javafx-web:21.0.10:win")
    implementation("org.openjfx:javafx-controls:21.0.10:win")
    implementation("org.openjfx:javafx-media:21.0.10:win")
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-cio:3.4.0")
    implementation("io.ktor:ktor-client-logging:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "com.score.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "aion2ScoreFinder"
            packageVersion = project.version.toString()
            copyright = "under MIT License"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("pythonScript"))

            windows {
                upgradeUuid = "ad6d277e-08d9-492a-ae6b-36427df321c6"   // 인터넷페이지에서 대충 만듬
                includeAllModules = true
                shortcut = true
                menu = true
                iconFile.set(project.file("src/main/resources/assets/icon.ico"))
            }
        }
    }
}

buildConfig {
    buildConfigField("String", "APP_VERSION", "\"${project.version}\"")
}

tasks.withType<JavaExec> {
    systemProperty("file.encoding", "UTF-8")
    systemProperty("console.encoding", "UTF-8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
