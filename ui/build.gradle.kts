plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(25)
}

compose.resources {
    packageOfResClass = "dev.cuervolu.loredesigner.ui.resources"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":workspace"))
    implementation(project(":editor"))
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.bundles.compose.unstyled)
    implementation(libs.compose.resources)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.compose.nav3)

    implementation(libs.koin.compose)
    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs)
    testImplementation(libs.compose.ui.test)
    testImplementation(compose.desktop.currentOs)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
}
