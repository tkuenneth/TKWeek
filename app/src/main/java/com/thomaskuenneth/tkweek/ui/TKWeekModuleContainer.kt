package com.thomaskuenneth.tkweek.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.thomaskuenneth.tkweek.TKWeekModule

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TKWeekModuleContainer(
    module: TKWeekModule,
    arguments: Bundle?,
    onResetScroll: () -> Unit
) {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
    val containerId = remember { View.generateViewId() }

    DisposableEffect(module, arguments) {
        val moduleName = module.clazz.name
        // Cleanup any existing fragment with this tag (e.g. restored from saved state)
        // to ensure we don't have duplicates or "zombies" attached to old view IDs.
        fragmentManager.findFragmentByTag(moduleName)?.let {
            fragmentManager.beginTransaction().remove(it).commit()
        }

        val fragment = module.clazz.getConstructor().newInstance() as Fragment
        fragment.arguments = arguments
        fragmentManager.beginTransaction()
            .replace(containerId, fragment, moduleName)
            .commit()

        onDispose {
            onResetScroll()
            fragmentManager.findFragmentByTag(moduleName)?.let {
                // Safe to use allowingStateLoss here because if the state is lost,
                // the fragment will be restored and then cleaned up by the block above
                // when the composable re-enters.
                fragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            FragmentContainerView(it).apply {
                id = containerId
            }
        }
    )
}
