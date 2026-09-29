plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.kotlinx.datetime)
    testImplementation(kotlin("test"))
}
