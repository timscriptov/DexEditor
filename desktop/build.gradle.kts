import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.mcal"
version = "1.0-SNAPSHOT"


kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DexEditor"
            packageVersion = "1.0.0"
            description = "DexEditor"
            copyright = "© 2023 timscriptov. All rights reserved."
            vendor = "timscriptov"
            windows {
                dirChooser = true
                // iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                shortcut = true
                menuGroup = packageName
            }
        }
    }
}
