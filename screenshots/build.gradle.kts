import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

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

            val framed = frameScreenshot(ImageIO.read(rawFile), deviceType)
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

fun frameScreenshot(source: BufferedImage, deviceType: String): BufferedImage {
    val w = source.width
    val h = source.height
    val border = (w * 0.035f).toInt()
    val topExtra = if (deviceType == "phone") (border * 1.8f).toInt() else border
    val outerRadius = w * 0.09f
    val innerRadius = (outerRadius - border * 0.6f).coerceAtLeast(8f)

    val totalW = w + border * 2
    val totalH = h + topExtra + border

    val result = BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_ARGB)
    val g = result.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    g.color = Color(0x2b, 0x2b, 0x2f)
    g.fill(RoundRectangle2D.Float(0f, 0f, totalW.toFloat(), totalH.toFloat(), outerRadius, outerRadius))

    g.color = Color(0x45, 0x45, 0x4a)
    g.stroke = BasicStroke((border * 0.08f).coerceAtLeast(2f))
    g.draw(
        RoundRectangle2D.Float(
            1f, 1f, totalW - 2f, totalH - 2f, outerRadius, outerRadius
        )
    )

    val screenClip = RoundRectangle2D.Float(
        border.toFloat(), topExtra.toFloat(), w.toFloat(), h.toFloat(), innerRadius, innerRadius
    )
    val oldClip = g.clip
    g.clip = screenClip
    g.drawImage(source, border, topExtra, null)
    g.clip = oldClip

    g.color = Color(0x00, 0x00, 0x00, 0x80)
    g.stroke = BasicStroke(2f)
    g.draw(screenClip)

    if (deviceType == "phone") {
        val camR = topExtra * 0.16f
        val camCx = totalW / 2f
        val camCy = topExtra / 2f
        g.color = Color(0x10, 0x10, 0x12)
        g.fill(Ellipse2D.Float(camCx - camR, camCy - camR, camR * 2, camR * 2))
        g.color = Color(0x3a, 0x3a, 0x40)
        g.stroke = BasicStroke(1.5f)
        g.draw(Ellipse2D.Float(camCx - camR, camCy - camR, camR * 2, camR * 2))
    } else {
        g.color = Color(0x00, 0x00, 0x00, 0x50)
        g.stroke = BasicStroke((border * 0.12f).coerceAtLeast(2f))
        g.drawLine(totalW / 2, 0, totalW / 2, totalH)
    }

    g.dispose()
    return result
}
