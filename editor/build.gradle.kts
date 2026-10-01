plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}
