/*
 * TKWeekCompose.kt
 *
 * Copyright 2022 - 2026 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.thomaskuenneth.tkweek

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.thomaskuenneth.tkweek.navigation.DEFAULT_MODULE
import com.thomaskuenneth.tkweek.navigation.applyNavigation
import com.thomaskuenneth.tkweek.navigation.canNavigateBack
import com.thomaskuenneth.tkweek.navigation.initialTKWeekBackStack
import com.thomaskuenneth.tkweek.navigation.isDetailPaneVisible
import com.thomaskuenneth.tkweek.navigation.isListPaneVisible
import com.thomaskuenneth.tkweek.types.TKWeekDestination
import com.thomaskuenneth.tkweek.ui.TKWeekModuleContainer
import com.thomaskuenneth.tkweek.ui.TKWeekModuleSelector
import com.thomaskuenneth.tkweek.ui.TKWeekTopAppBar
import com.thomaskuenneth.tkweek.ui.colorScheme
import com.thomaskuenneth.tkweek.util.Helper.CLAZZ
import com.thomaskuenneth.tkweek.viewmodel.NavigationRequest
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max

@AndroidEntryPoint
class TKWeekCompose : AppCompatActivity() {

    private val viewModel: TKWeekViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deepLinkRequest = intent.toNavigationRequest()
        enableEdgeToEdge()
        setContent {
            TKWeekApp(viewModel = viewModel, initialNavigationRequest = deepLinkRequest)
        }
    }

    // FLAG_ACTIVITY_CLEAR_TOP redelivers a widget tap's Intent here instead of a new onCreate()
    // when the task is already running; without this override that Intent is silently dropped.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.toNavigationRequest()?.let { request ->
            viewModel.requestNavigation(module = request.module, topLevel = true)
        }
    }

    private fun Intent.toNavigationRequest(): NavigationRequest? =
        getStringExtra(CLAZZ)?.let { clazzName ->
            TKWeekModule.entries.firstOrNull { it.clazz.name == clazzName }
        }?.let { module ->
            NavigationRequest(module = module, topLevel = true)
        }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
fun TKWeekApp(
    viewModel: TKWeekViewModel = viewModel(),
    initialNavigationRequest: NavigationRequest? = null,
) {
    MaterialTheme(
        colorScheme = colorScheme()
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val appBarActions by viewModel.appBarActions.collectAsState()

        val directive = calculatePaneScaffoldDirective(
            windowAdaptiveInfo = currentWindowAdaptiveInfo(),
            verticalHingePolicy = if (uiState.avoidHinge) HingePolicy.AlwaysAvoid else HingePolicy.NeverAvoid
        )
        val isTwoPane = directive.maxHorizontalPartitions > 1

        val backStack = rememberNavBackStack(
            *initialTKWeekBackStack().toMutableList()
                .apply { initialNavigationRequest?.let { applyNavigation(it) } }
                .toTypedArray()
        )

        LaunchedEffect(Unit) {
            viewModel.navigationRequests.collect { request ->
                backStack.applyNavigation(request)
            }
        }

        val navigatedModule = (backStack.lastOrNull() as? TKWeekDestination.Detail)?.module
        val selectedModule = navigatedModule ?: DEFAULT_MODULE.takeIf { isTwoPane }
        val activeModuleTitleRes = selectedModule?.titleRes ?: R.string.app_name
        val detailVisible = isDetailPaneVisible(isTwoPane, backStack.size)
        val listVisible = isListPaneVisible(isTwoPane, backStack.size)
        val canGoBack = canNavigateBack(isTwoPane, backStack.size)
        val activity = LocalActivity.current
        // PopLatest pops exactly one entry, which is correct for nesting inside the detail pane;
        // once there's nothing left that canNavigateBack considers a real back-target (the two-pane
        // top-level selection isn't one - see TKWeekPaneVisibility), back should exit like any
        // single-Activity app rather than popping into an inconsistent or self-reset state.
        val onNavigateBack: () -> Unit = {
            if (canGoBack) {
                backStack.removeLastOrNull()
            } else {
                activity?.finish()
            }
        }

        LaunchedEffect(listVisible) {
            if (!listVisible) {
                viewModel.setListScrolled(false)
            }
        }

        val sceneStrategy = rememberListDetailSceneStrategy<NavKey>(
            backNavigationBehavior = BackNavigationBehavior.PopLatest,
            directive = directive,
        )

        val displayCutoutInsets = WindowInsets.displayCutout
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val left = displayCutoutInsets.getLeft(density, layoutDirection)
        val right = displayCutoutInsets.getRight(density, layoutDirection)
        val horizontalPadding =
            with(density) { max(left, right).toDp() }.coerceAtLeast(16.dp)

        Scaffold(
            contentWindowInsets = WindowInsets(),
            topBar = {
                TKWeekTopAppBar(
                    uiState = uiState,
                    detailVisible = detailVisible,
                    activeModuleTitleRes = activeModuleTitleRes,
                    appBarActions = appBarActions,
                    canNavigateBack = canGoBack,
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = horizontalPadding),
                onBack = onNavigateBack,
                sceneStrategies = listOf(sceneStrategy),
                entryProvider = entryProvider {
                    entry<TKWeekDestination.ModuleList>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = {
                                ModuleDetail(
                                    module = DEFAULT_MODULE,
                                    date = null,
                                    showProgressIndicator = uiState.shouldShowProgressIndicator,
                                )
                            }
                        )
                    ) {
                        TKWeekModuleSelector(
                            selectedModule = selectedModule,
                            onModuleSelected = { module ->
                                viewModel.requestNavigation(module = module, topLevel = true)
                            },
                            onListStateChanged = { isAtTop ->
                                viewModel.setListScrolled(!isAtTop)
                            }
                        )
                    }
                    entry<TKWeekDestination.Detail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { detail ->
                        ModuleDetail(
                            module = detail.module,
                            date = detail.date,
                            showProgressIndicator = uiState.shouldShowProgressIndicator,
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ModuleDetail(module: TKWeekModule, date: Long?, showProgressIndicator: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        TKWeekModuleContainer(module = module, date = date)
        if (showProgressIndicator) {
            CircularProgressIndicator()
        }
    }
}
