plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.nucleus) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
}

allprojects {
    group = "dev.cuervolu"
    version = "0.1.0"
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    kover(project(":app"))
    kover(project(":core"))
    kover(project(":workspace"))
    kover(project(":platform"))
    kover(project(":editor"))
    kover(project(":ui"))
}
