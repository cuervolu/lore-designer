plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.kotlinx.datetime)
    testImplementation(kotlin("test"))
}
