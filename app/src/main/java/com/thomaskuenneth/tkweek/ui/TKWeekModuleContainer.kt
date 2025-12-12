package com.thomaskuenneth.tkweek.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.viewmodel.UiState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ThreePaneScaffoldPaneScope.TKWeekModuleContainer(
    uiState: UiState
) {
    AnimatedPane {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                FragmentContainerView(context).apply {
                    id = R.id.fragment_container_view
                }
            },
            update = { view ->
                with(uiState.modules.last()) {
                    val fragmentManager = (view.context as AppCompatActivity).supportFragmentManager
                    val fragment =
                        module.clazz.getConstructor()
                            .newInstance() as Fragment
                    fragment.arguments = arguments
                    fragmentManager.beginTransaction()
                        .replace(view.id, fragment)
                        .commit()
                }
            }
        )
    }
}
