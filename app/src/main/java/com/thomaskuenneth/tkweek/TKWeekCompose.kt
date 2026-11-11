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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.thomaskuenneth.tkweek.navigation.DEFAULT_MODULE
import com.thomaskuenneth.tkweek.navigation.applyNavigation
import com.thomaskuenneth.tkweek.navigation.canNavigateBack
import com.thomaskuenneth.tkweek.navigation.initialTKWeekBackStack
import com.thomaskuenneth.tkweek.types.TKWeekDestination
import com.thomaskuenneth.tkweek.ui.TKWeekModuleContainer
import com.thomaskuenneth.tkweek.ui.TKWeekModuleSelector
import com.thomaskuenneth.tkweek.ui.TKWeekTopAppBar
import com.thomaskuenneth.tkweek.ui.colorScheme
import com.thomaskuenneth.tkweek.ui.fadingEdge
import com.thomaskuenneth.tkweek.util.Helper.CLAZZ
import com.thomaskuenneth.tkweek.viewmodel.NavigationRequest
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel
import dagger.hilt.android.AndroidEntryPoint

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
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val directive = calculatePaneScaffoldDirective(
            windowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
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

        val sceneStrategy = rememberListDetailSceneStrategy<NavKey>(
            backNavigationBehavior = BackNavigationBehavior.PopLatest,
            directive = directive,
        )

        val horizontalInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val left = horizontalInsets.getLeft(density, layoutDirection)
        val right = horizontalInsets.getRight(density, layoutDirection)
        val contentPadding = dimensionResource(R.dimen.content_padding)
        val leftPadding = with(density) { left.toDp() } + contentPadding
        val rightPadding = with(density) { right.toDp() } + contentPadding

        Scaffold(
            contentWindowInsets = WindowInsets(),
        ) { paddingValues ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .padding(paddingValues)
                    .absolutePadding(left = leftPadding, right = rightPadding),
                onBack = onNavigateBack,
                sceneStrategies = listOf(sceneStrategy),
                entryProvider = entryProvider {
                    entry<TKWeekDestination.ModuleList>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = {
                                DetailPane(
                                    module = DEFAULT_MODULE,
                                    date = null,
                                    showProgressIndicator = uiState.shouldShowProgressIndicator,
                                    canNavigateBack = canGoBack,
                                    onNavigateBack = onNavigateBack,
                                )
                            }
                        )
                    ) {
                        TKWeekModuleSelector(
                            selectedModule = selectedModule,
                            onModuleSelected = { module ->
                                viewModel.requestNavigation(module = module, topLevel = true)
                            }
                        )
                    }
                    entry<TKWeekDestination.Detail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { detail ->
                        DetailPane(
                            module = detail.module,
                            date = detail.date,
                            showProgressIndicator = uiState.shouldShowProgressIndicator,
                            canNavigateBack = canGoBack,
                            onNavigateBack = onNavigateBack,
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailPane(
    module: TKWeekModule,
    date: Long?,
    showProgressIndicator: Boolean,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
) {
    Column {
        AnimatedVisibility(visible = canNavigateBack) {
            TKWeekTopAppBar(
                titleRes = module.titleRes,
                onNavigateBack = onNavigateBack,
            )
        }
        ModuleDetail(
            module = module,
            date = date,
            showProgressIndicator = showProgressIndicator,
        )
    }
}

@Composable
private fun ModuleDetail(module: TKWeekModule, date: Long?, showProgressIndicator: Boolean) {
    Box(
        modifier = Modifier.fadingEdge(),
        contentAlignment = Alignment.Center
    ) {
        TKWeekModuleContainer(module = module, date = date)
        if (showProgressIndicator) {
            CircularProgressIndicator()
        }
    }
}
