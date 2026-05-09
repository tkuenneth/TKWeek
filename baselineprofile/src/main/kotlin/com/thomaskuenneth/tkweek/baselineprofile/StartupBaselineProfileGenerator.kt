package com.thomaskuenneth.tkweek.baselineprofile

import android.content.res.Resources
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = true,
        maxIterations = 12,
        stableIterations = 3
    ) {
        val labels = targetUiLabels()
        pressHome()
        startActivityAndWait()
        awaitAppInForeground()
        awaitAnyText(labels.primaryModuleTitle, labels.calendarTitle)
        openModule(labels.calendarTitle)
        awaitText(labels.calendarTitle)
        device.pressBack()
        awaitAnyText(labels.primaryModuleTitle, labels.calendarTitle)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.awaitAppInForeground(timeoutMs: Long = 8_000) {
        check(device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), timeoutMs)) {
            "Target app package '$TARGET_PACKAGE' not visible within $timeoutMs ms"
        }
    }

    private fun MacrobenchmarkScope.awaitText(text: String, timeoutMs: Long = 5_000) {
        check(device.wait(Until.hasObject(By.text(text)), timeoutMs)) {
            "Expected text '$text' not visible within $timeoutMs ms"
        }
    }

    private fun MacrobenchmarkScope.awaitAnyText(
        first: String,
        second: String,
        timeoutMs: Long = 6_000
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (device.hasObject(By.text(first)) || device.hasObject(By.text(second))) {
                return
            }
            device.waitForIdle(200)
        }
        error("Neither '$first' nor '$second' became visible within $timeoutMs ms")
    }

    private fun MacrobenchmarkScope.openModule(moduleTitle: String) {
        val moduleNode = device.findObject(By.text(moduleTitle))
        check(moduleNode != null) { "Module '$moduleTitle' was not found in module list" }
        moduleNode.click()
        device.waitForIdle()
    }

    private data class UiLabels(
        val primaryModuleTitle: String,
        val calendarTitle: String
    )

    private fun targetUiLabels(): UiLabels {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetPackageName = InstrumentationRegistry.getArguments()
            .getString(TARGET_PACKAGE_ARG)
            ?.takeIf { it.isNotBlank() }
            ?: TARGET_PACKAGE
        val resources =
            instrumentation.context.packageManager.getResourcesForApplication(targetPackageName)
        return UiLabels(
            primaryModuleTitle = stringFromTargetResources(
                "week_activity_text1",
                resources,
                targetPackageName
            ),
            calendarTitle = stringFromTargetResources(
                "calendar_activity_text1",
                resources,
                targetPackageName
            )
        )
    }

    private fun stringFromTargetResources(
        name: String,
        resources: Resources,
        packageName: String
    ): String {
        val id = resources.getIdentifier(name, "string", packageName)
        check(id != 0) { "Missing target string resource: $name" }
        return resources.getString(id)
    }

    private companion object {
        const val TARGET_PACKAGE_ARG = "androidx.benchmark.targetPackageName"
        const val TARGET_PACKAGE = "com.thomaskuenneth.tkweek"
    }
}
