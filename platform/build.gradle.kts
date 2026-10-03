plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":workspace"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.properties)
    implementation(libs.semver)
    implementation(libs.koin.core)

    implementation(libs.filekit.core)

    implementation(libs.multiplatformSettings)
    implementation(libs.ktoml)
    implementation(libs.sqlite.bundled)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okio.fakefilesystem)
    testImplementation(libs.multiplatformSettings.test)
    api(libs.kermit)
    implementation(libs.nucleus.core.runtime)
    implementation(libs.nucleus.system.info)
    implementation(libs.room.runtime)

    add("ksp", libs.room.compiler)
    testImplementation(libs.koin.test)
    testImplementation(kotlin("test"))
}

room {
    schemaDirectory("$projectDir/schemas")
}
