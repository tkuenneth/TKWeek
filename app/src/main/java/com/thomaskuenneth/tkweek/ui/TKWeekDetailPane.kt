package com.thomaskuenneth.tkweek.ui

import android.os.Bundle
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.viewmodel.UiState

private const val ARGUMENTS = "arguments"

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ThreePaneScaffoldPaneScope.TKWeekDetailPane(
    uiState: UiState,
    navController: NavHostController,
    detailVisible: Boolean,
) {
    AnimatedPane {
        val startDestination = uiState.topLevelModuleWithArguments.module.name
        key(startDestination) {
            val content: @Composable () -> Unit = {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    TKWeekModule.entries.forEach { moduleEntry ->
                        composable(
                            route = moduleEntry.name,
                            arguments = listOf(navArgument(ARGUMENTS) {
                                type = NavType.ParcelableType(Bundle::class.java)
                                nullable = true
                            }),
                            enterTransition = {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) + fadeIn()
                            },
                            exitTransition = {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) + fadeOut()
                            },
                            popEnterTransition = {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) + fadeIn()
                            },
                            popExitTransition = {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) + fadeOut()
                            }
                        ) {
                            val args =
                                navController.previousBackStackEntry?.savedStateHandle?.get<Bundle>(
                                    ARGUMENTS
                                )

                            Box(contentAlignment = Alignment.Center) {
                                TKWeekModuleContainer(
                                    module = moduleEntry,
                                    arguments = args,
                                )
                                if (uiState.shouldShowProgressIndicator) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
            if (detailVisible) {
                val visibleState = remember {
                    MutableTransitionState(false).apply {
                        targetState = true
                    }
                }
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = slideInHorizontally { it }
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}
