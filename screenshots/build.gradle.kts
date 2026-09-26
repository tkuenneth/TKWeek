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
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "com.thomaskuenneth.tkweek.screenshots.HiltTestRunner"
    }

    targetProjectPath = ":app"

    buildTypes {
        // Variant matching pairs this module's build type with the app build type of the same
        // name, so the app's "screenshots" type has to exist here too.
        create("screenshots") {
            initWith(getByName("debug"))
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// Only the screenshots variant is meaningful here; a debug variant would target the
// debug app rather than the release-derived one the store images must show.
androidComponents {
    beforeVariants { variant ->
        variant.enable = variant.buildType == "screenshots"
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
    // The screenshots build type is a signed copy of release, so the captures show the app
    // as it ships rather than a debug build.
    dependsOn(":app:installScreenshots", "installScreenshots")

    // This project has the configuration cache enabled, so everything the action needs is
    // captured here as plain values. Neither Project nor build-script functions may be
    // referenced from doLast - hence the local helpers below.
    val rootDirectory = project.rootDir
    val rawDirectory = layout.buildDirectory.dir("store-screenshots/raw").get().asFile
    val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
    val sequence = storeScreenshotSequence

    doLast {
        fun runCommand(vararg command: String): String {
            val process = ProcessBuilder(*command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw GradleException(
                    "Command failed (exit $exitCode): ${command.joinToString(" ")}\n$output"
                )
            }
            return output.trim()
        }

        fun displaySize(layoutFile: File): Pair<Int, Int>? {
            val body = Regex("display\\s*\\{([^}]*)}").find(layoutFile.readText())
                ?.groupValues?.get(1) ?: return null
            val w = Regex("width\\s+(\\d+)").find(body)?.groupValues?.get(1)?.toInt()
            val h = Regex("height\\s+(\\d+)").find(body)?.groupValues?.get(1)?.toInt()
            return if (w == null || h == null) null else w to h
        }

        // Resolve the skin from the captured image rather than hardcoding a device: the
        // screenshot is composited at native size, so only a skin whose display matches can
        // frame it correctly. Where several skins share a resolution, prefer the one whose
        // folder name the AVD is named after.
        fun skinFor(width: Int, height: Int, sdkDirectory: String, avdName: String): File {
            val skinsRoot = File(sdkDirectory, "skins")
            val layouts = skinsRoot.listFiles().orEmpty().sortedBy { it.name }.flatMap { dir ->
                listOf(File(dir, "layout"), File(dir, "default/layout"))
            }.filter { it.isFile }
            val fitting = layouts.filter { displaySize(it) == width to height }
            check(fitting.isNotEmpty()) {
                "No emulator skin under $skinsRoot has a ${width}x$height display"
            }
            val avd = avdName.lowercase().replace(' ', '_')
            return (fitting.firstOrNull { layout ->
                val name = generateSequence(layout.parentFile) { it.parentFile }
                    .first { it.parentFile?.name == "skins" }.name
                avd.startsWith(name)
            } ?: fitting.first()).parentFile
        }

        // canvasWidth, canvasHeight, deviceX, deviceY
        fun parseSkinLayout(layoutFile: File): IntArray {
            val text = layoutFile.readText()
            val layoutsIndex = text.indexOf("layouts {")
            require(layoutsIndex >= 0) { "No 'layouts' block found in $layoutFile" }
            val layoutsSection = text.substring(layoutsIndex)

            val canvasWidth = Regex("width\\s+(\\d+)").find(layoutsSection)!!.groupValues[1].toInt()
            val canvasHeight = Regex("height\\s+(\\d+)").find(layoutsSection)!!.groupValues[1].toInt()

            val part2Body = Regex("part2\\s*\\{([^}]*)}").find(layoutsSection)!!.groupValues[1]
            val deviceX = Regex("x\\s+(\\d+)").find(part2Body)!!.groupValues[1].toInt()
            val deviceY = Regex("y\\s+(\\d+)").find(part2Body)!!.groupValues[1].toInt()

            return intArrayOf(canvasWidth, canvasHeight, deviceX, deviceY)
        }

        fun frameScreenshot(
            source: BufferedImage,
            sdkDirectory: String,
            avdName: String
        ): BufferedImage {
            ImageIO.scanForPlugins()
            val skinDir = skinFor(source.width, source.height, sdkDirectory, avdName)
            val layoutValues = parseSkinLayout(File(skinDir, "layout"))
            val canvasWidth = layoutValues[0]
            val canvasHeight = layoutValues[1]
            val deviceX = layoutValues[2]
            val deviceY = layoutValues[3]

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

            val canvas = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)
            val g = canvas.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            g.drawImage(back, 0, 0, canvasWidth, canvasHeight, null)
            g.drawImage(maskedSource, deviceX, deviceY, source.width, source.height, null)
            g.dispose()

            return canvas
        }

        // A capture can come back all black when the emulator's graphics service is unhealthy
        // (it crash-loops as "android.hardware" in logcat). The test still passes, because the
        // view hierarchy is fine, so nothing else notices. Only the app's own area is sampled:
        // the status bar and gesture handle carry enough colours to mask a black window.
        fun isBlank(file: File): Boolean {
            val image = ImageIO.read(file)
                ?: throw GradleException("Could not read the capture ${file.name}")
            val colours = mutableSetOf<Int>()
            var y = image.height * 8 / 100
            val bottom = image.height * 92 / 100
            while (y < bottom && colours.size < 16) {
                var x = 0
                while (x < image.width && colours.size < 16) {
                    colours.add(image.getRGB(x, y))
                    x += 4
                }
                y += 4
            }
            return colours.size < 16
        }

        val sdkDir = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
            ?: throw GradleException("Set ANDROID_HOME (or ANDROID_SDK_ROOT) to your Android SDK location")
        val adbName = if (isWindows) "adb.exe" else "adb"
        val adb = File(sdkDir, "platform-tools/$adbName").absolutePath
        val appId = "com.thomaskuenneth.tkweek"
        val testAppId = "com.thomaskuenneth.tkweek.screenshots"
        val runner = "com.thomaskuenneth.tkweek.screenshots.HiltTestRunner"

        // The GIF script runs at the very end, but what it needs is checked now: finding out
        // about a missing tool after the capture run has already happened wastes minutes.
        val bash = if (!isWindows) "bash" else {
            // "bash" on the Windows PATH is the WSL stub, which cannot see the Windows paths
            // these scripts build. Use Git for Windows' bash, which sits next to git.exe.
            val gitDirectory = System.getenv("PATH").orEmpty().split(File.pathSeparator)
                .map { File(it, "git.exe") }.firstOrNull { it.isFile }?.parentFile?.parentFile
            listOfNotNull(
                gitDirectory?.resolve("bin/bash.exe"),
                File("C:/Program Files/Git/bin/bash.exe")
            ).firstOrNull { it.isFile }?.absolutePath
                ?: throw GradleException("Git Bash was not found; it runs the GIF scripts.")
        }
        val magickProbe = ProcessBuilder(bash, "-lc", "command -v magick")
            .redirectErrorStream(true).start()
        check(magickProbe.waitFor() == 0) {
            "ImageMagick is not on the PATH. The GIF scripts need its 'magick' command - " +
                "install it (winget install ImageMagick.ImageMagick) and run this task again."
        }

        // Emulators report the AVD they were started from; used only to pick between skins
        // that share a display resolution.
        val avdName = runCommand(adb, "shell", "getprop", "ro.boot.qemu.avd_name")

        runCommand(
            adb, "shell", "am", "instrument", "-w",
            "-e", "class", "com.thomaskuenneth.tkweek.screenshots.StoreScreenshotTest",
            "$testAppId/$runner"
        )

        rawDirectory.deleteRecursively()
        rawDirectory.mkdirs()
        runCommand(
            adb, "pull",
            "/sdcard/Android/data/$appId/files/store-screenshots/.",
            rawDirectory.absolutePath
        )

        val rawFiles = rawDirectory.listFiles()?.toList().orEmpty()
        check(rawFiles.isNotEmpty()) {
            "No screenshots found under ${rawDirectory.relativeTo(rootDirectory)} - did StoreScreenshotTest run?"
        }
        rawFiles.filter { it.extension == "png" }.firstOrNull { isBlank(it) }?.let {
            throw GradleException(
                "The capture ${it.name} is blank, so the committed store screenshots were left " +
                    "alone. The emulator's graphics service most likely died - check " +
                    "'adb logcat' for android.hardware crashes, then cold boot the AVD."
            )
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

        val namesForDevice = if (isPhone) sequence else sequence.drop(1)
        val firstRawIndex = if (isPhone) 0 else 1
        val outputDir = File(rootDirectory, "fastlane/metadata/android/en-US/images/$outputDirName")
        outputDir.mkdirs()

        namesForDevice.forEachIndexed { position, name ->
            val rawIndex = firstRawIndex + position
            val rawFile = rawFiles.firstOrNull { it.name.startsWith("%02d_".format(rawIndex)) }
                ?: throw GradleException("Missing captured screenshot for '$name' (index $rawIndex)")

            val framed = frameScreenshot(ImageIO.read(rawFile), sdkDir, avdName)
            val outputFile = File(outputDir, "%02d.png".format(position + 1))
            ImageIO.write(framed, "png", outputFile)
            logger.lifecycle("Wrote ${outputFile.relativeTo(rootDirectory)}")
        }

        // Rebuild the animated GIF for this form factor - only the one that reads the
        // directory this run just wrote. The other is built from screenshots that did not
        // change, so rebuilding it would be pointless work and would leave a modified file
        // with no actual difference in it.
        val gifScript = if (deviceType == "fold") "create_foldable_gif.sh" else "create_gif.sh"
        File(rootDirectory, gifScript).let { script ->
            check(script.isFile) { "Missing $gifScript in ${rootDirectory.name}" }
            logger.lifecycle("Running $gifScript")
            // Forward slashes: Git Bash resolves "C:/..." reliably, a backslash path not.
            // On macOS this is already the path's own form, so the same call works there.
            runCommand(bash, script.absolutePath.replace('\\', '/'))
        }
    }
}
