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
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(25)
}

compose.resources {
    packageOfResClass = "dev.cuervolu.loredesigner.resources"
}

val appNameStr = "Lore Designer"
val appId = "dev.cuervolu.loredesigner"
val vendorName = "Cuervolu"
val linuxPackageName = "lore-designer"
val maintainer = "Cuervolu <contact@cuervolu.dev>"
val appIcons = rootProject.file("appIcons")
val githubOwner = "cuervolu"
val githubRepository = "lore-designer"
val homepageUrl = "https://github.com/$githubOwner/$githubRepository"

nucleus.application {
    mainClass = "$appId.MainKt"

    nativeDistributions {
        enableAotCache = true
        cleanupNativeLibs = true

        targetFormats(
            TargetFormat.Dmg,
            TargetFormat.Zip,
            TargetFormat.Nsis,
            TargetFormat.Deb,
            TargetFormat.AppImage,
        )

        packageName = appNameStr
        appName = appNameStr
        packageVersion = version.toString()

        description =
            "A desktop worldbuilding tool for developing characters, places, relationships, and complex fictional worlds."

        vendor = vendorName
        copyright = "Copyright ${Year.now().value} $vendorName."
        homepage = homepageUrl

        artifactName = $$"${name}-${version}-${os}-${arch}.${ext}"

        windows {
            iconFile.set(appIcons.resolve("icon.ico"))
            upgradeUuid = "b46ef0f1-2840-46c4-8703-42f9bf18d175"
            console = false

            nsis {
                oneClick = false
                allowElevation = true
                perMachine = false
                allowToChangeInstallationDirectory = true

                createDesktopShortcut = true
                runAfterFinish = true

                shortcutName = appNameStr
                menuCategory = vendorName
            }
        }

        macOS {
            iconFile.set(appIcons.resolve("icon.icns"))
            dockName = appNameStr
            bundleID = appId
            appCategory = "public.app-category.productivity"
        }

        linux {
            iconFile.set(appIcons.resolve("icon.png"))
            packageName = linuxPackageName

            appCategory = "Office"
            menuGroup = "Office"
            shortcut = true

            debMaintainer = maintainer

            modules("jdk.security.auth")
        }

        publish {
            github {
                enabled = true
                owner = githubOwner
                repo = githubRepository
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
    implementation(project(":core"))
    implementation(project(":ui"))
    implementation(project(":workspace"))
    implementation(project(":platform"))
    implementation(project(":editor"))
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.resources)
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
