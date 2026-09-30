import dev.nucleusframework.desktop.application.dsl.ReleaseChannel
import dev.nucleusframework.desktop.application.dsl.ReleaseType
import dev.nucleusframework.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.AbstractComposeHotRun
import java.time.Year

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.nucleus)
}

kotlin {
    jvmToolchain(25)
}

nucleus.application {
    mainClass = "dev.cuervolu.loredesigner.MainKt"

    nativeDistributions {
        enableAotCache = true
        cleanupNativeLibs = true
        appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/appResources"))

        targetFormats(
            TargetFormat.Dmg,
            TargetFormat.Pkg,
            TargetFormat.Zip, // required alongside DMG for macOS updater
            TargetFormat.Nsis,
            TargetFormat.Deb,
            TargetFormat.AppImage,
        )

        packageName = "Lore Designer"
        appName = "Lore Designer"
        packageVersion = version.toString()

        description =
            "A desktop worldbuilding tool for developing characters, places, relationships, and complex fictional worlds."

        vendor = "Cuervolu"
        copyright = "Copyright ${Year.now().value} Cuervolu."
        homepage = "https://github.com/cuervolu/lore-designer"

        artifactName = $$"${name}-${version}-${os}-${arch}.${ext}"

        windows {
            iconFile.set(rootProject.file("appIcons/WindowsIcon.ico"))
            upgradeUuid = "b46ef0f1-2840-46c4-8703-42f9bf18d175"
            console = false
            menuGroup = "Cuervolu"
        }

        linux {
            iconFile.set(rootProject.file("appIcons/LinuxIcon.png"))
            appCategory = "Office"
            menuGroup = "Office"
            debMaintainer = "angel.cuervo187@gmail.com"
            modules("jdk.security.auth")
        }

        publish {
            github {
                enabled = true
                owner = "cuervolu"
                repo = "lore-designer"
                token = System.getenv("GITHUB_TOKEN") ?: ""
                channel = ReleaseChannel.Latest
                releaseType = ReleaseType.Release
            }
        }
    }
}

tasks.withType<AbstractComposeHotRun> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":workspace"))
    implementation(project(":platform"))
    implementation(project(":editor"))
    implementation(compose.desktop.currentOs)
    implementation(libs.nucleus.application)
    implementation(libs.nucleus.decorated.window.tao)
    implementation(libs.nucleus.darkmode.detector)
    implementation(libs.nucleus.core.runtime)
    implementation(libs.koin.core)
    implementation(libs.filekit.core)
    implementation(libs.kotlinx.coroutines.swing)
    testImplementation(libs.koin.test)
    testImplementation(kotlin("test"))
}
