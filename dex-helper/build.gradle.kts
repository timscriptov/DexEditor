plugins {
    id("java")
    kotlin("jvm")
}

group = "org.mcal"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://www.jitpack.io" ) }
    maven { url = uri("https://www.jabylon.org/maven/" ) }
}

dependencies {
    implementation("org.smali:dexlib2:2.5.2")
    implementation("org.smali:smali:2.5.2")
    implementation("org.smali:baksmali:2.5.2")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

//java {
//    sourceCompatibility = JavaVersion.VERSION_17
//    targetCompatibility = JavaVersion.VERSION_17
//}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}