import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.awt.AlphaComposite
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.sejda.imageio:webp-imageio:0.1.6")
    }
}

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.thomaskuenneth.tkweek.screenshots"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "com.thomaskuenneth.tkweek.screenshots.HiltTestRunner"
    }

    targetProjectPath = ":app"

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.ui.test.junit4)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.window)
    implementation(libs.hilt.android)
    implementation(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)
}

val storeScreenshotSequence = listOf(
    "module_list", "week", "my_day", "days_between_dates", "date_calculator",
    "events", "about_a_year", "calendar", "settings", "about",
)

tasks.register("generateStoreScreenshots") {
    group = "screenshots"
    description = "Captures and frames F-Droid/Play store screenshots from the currently " +
        "running emulator/device. Phone vs. foldable is detected from the device's own screen."
    dependsOn(":app:installDebug", "installDebug")

    doLast {
        val sdkDir = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
            ?: throw GradleException("Set ANDROID_HOME (or ANDROID_SDK_ROOT) to your Android SDK location")
        val adbName = if (org.gradle.internal.os.OperatingSystem.current().isWindows) "adb.exe" else "adb"
        val adb = File(sdkDir, "platform-tools/$adbName").absolutePath
        val appId = "com.thomaskuenneth.tkweek"
        val testAppId = "com.thomaskuenneth.tkweek.screenshots"
        val runner = "com.thomaskuenneth.tkweek.screenshots.HiltTestRunner"

        runCommand(
            adb, "shell", "am", "instrument", "-w",
            "-e", "class", "com.thomaskuenneth.tkweek.screenshots.StoreScreenshotTest",
            "$testAppId/$runner"
        )

        val rawDir = layout.buildDirectory.dir("store-screenshots/raw").get().asFile
        rawDir.deleteRecursively()
        rawDir.mkdirs()
        runCommand(
            adb, "pull",
            "/sdcard/Android/data/$appId/files/store-screenshots/.",
            rawDir.absolutePath
        )

        val rawFiles = rawDir.listFiles()?.toList().orEmpty()
        check(rawFiles.isNotEmpty()) {
            "No screenshots found under ${rawDir.relativeTo(rootDir)} - did StoreScreenshotTest run?"
        }

        val deviceClassFile = rawFiles.firstOrNull { it.name == "device-class.txt" }
            ?: throw GradleException("Missing device-class.txt - did StoreScreenshotTest run?")
        val deviceType = deviceClassFile.readText().trim()
        val isPhone = deviceType == "phone"
        val outputDirName = when (deviceType) {
            "phone" -> "phoneScreenshots"
            "fold" -> "sevenInchScreenshots"
            else -> throw GradleException(
                "No output folder configured for device class '$deviceType' yet"
            )
        }

        val namesForDevice = if (isPhone) storeScreenshotSequence else storeScreenshotSequence.drop(1)
        val firstRawIndex = if (isPhone) 0 else 1
        val outputDir = rootProject.file(
            "fastlane/metadata/android/en-US/images/$outputDirName"
        )
        outputDir.mkdirs()

        namesForDevice.forEachIndexed { position, name ->
            val rawIndex = firstRawIndex + position
            val rawFile = rawFiles.firstOrNull { it.name.startsWith("%02d_".format(rawIndex)) }
                ?: throw GradleException("Missing captured screenshot for '$name' (index $rawIndex)")

            val framed = frameScreenshot(ImageIO.read(rawFile), deviceType, sdkDir)
            val outputFile = File(outputDir, "%02d.png".format(position + 1))
            ImageIO.write(framed, "png", outputFile)
            logger.lifecycle("Wrote ${outputFile.relativeTo(rootDir)}")
        }
    }
}

fun runCommand(vararg command: String) {
    val process = ProcessBuilder(*command).redirectErrorStream(true).start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw GradleException("Command failed (exit $exitCode): ${command.joinToString(" ")}\n$output")
    }
}

data class SkinLayout(val canvasWidth: Int, val canvasHeight: Int, val deviceX: Int, val deviceY: Int)

fun parseSkinLayout(layoutFile: File): SkinLayout {
    val text = layoutFile.readText()
    val layoutsIndex = text.indexOf("layouts {")
    require(layoutsIndex >= 0) { "No 'layouts' block found in $layoutFile" }
    val layoutsSection = text.substring(layoutsIndex)

    val canvasWidth = Regex("width\\s+(\\d+)").find(layoutsSection)!!.groupValues[1].toInt()
    val canvasHeight = Regex("height\\s+(\\d+)").find(layoutsSection)!!.groupValues[1].toInt()

    val part2Body = Regex("part2\\s*\\{([^}]*)}").find(layoutsSection)!!.groupValues[1]
    val deviceX = Regex("x\\s+(\\d+)").find(part2Body)!!.groupValues[1].toInt()
    val deviceY = Regex("y\\s+(\\d+)").find(part2Body)!!.groupValues[1].toInt()

    return SkinLayout(canvasWidth, canvasHeight, deviceX, deviceY)
}

fun frameScreenshot(source: BufferedImage, deviceType: String, sdkDir: String): BufferedImage {
    ImageIO.scanForPlugins()
    val skinDir = when (deviceType) {
        "phone" -> File(sdkDir, "skins/pixel_10")
        "fold" -> File(sdkDir, "skins/pixel_fold/default")
        else -> throw GradleException("No SDK skin configured for device class '$deviceType'")
    }
    val layout = parseSkinLayout(File(skinDir, "layout"))
    val back = ImageIO.read(File(skinDir, "back.webp"))
        ?: throw GradleException("Could not decode ${File(skinDir, "back.webp")}")
    val mask = ImageIO.read(File(skinDir, "mask.webp"))
        ?: throw GradleException("Could not decode ${File(skinDir, "mask.webp")}")

    val maskedSource = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
    val sg = maskedSource.createGraphics()
    sg.drawImage(source, 0, 0, source.width, source.height, null)
    sg.composite = AlphaComposite.DstOut
    sg.drawImage(mask, 0, 0, source.width, source.height, null)
    sg.dispose()

    val canvas = BufferedImage(layout.canvasWidth, layout.canvasHeight, BufferedImage.TYPE_INT_ARGB)
    val g = canvas.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(back, 0, 0, layout.canvasWidth, layout.canvasHeight, null)
    g.drawImage(maskedSource, layout.deviceX, layout.deviceY, source.width, source.height, null)
    g.dispose()

    return canvas
}
