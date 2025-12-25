package com.thomaskuenneth.tkweek

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thomaskuenneth.tkweek.ui.TKWeekDetailPane
import com.thomaskuenneth.tkweek.ui.TKWeekModuleSelector
import com.thomaskuenneth.tkweek.ui.TKWeekTopAppBar
import com.thomaskuenneth.tkweek.ui.colorScheme
import com.thomaskuenneth.tkweek.util.Helper.CLAZZ
import com.thomaskuenneth.tkweek.util.Helper.PAYLOAD
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.max

@AndroidEntryPoint
class TKWeekCompose : AppCompatActivity() {

    private val viewModel: TKWeekViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.getStringExtra(CLAZZ)?.let { clazzName ->
            TKWeekModule.entries.firstOrNull { it.clazz.name == clazzName }?.let {
                viewModel.selectModuleWithArguments(
                    module = it,
                    arguments = intent?.getBundleExtra(PAYLOAD),
                    topLevel = true
                )
            }
        }
        enableEdgeToEdge()
        setContent {
            TKWeekApp(viewModel)
        }
    }
}

private const val ARGUMENTS = "arguments"

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun TKWeekApp(viewModel: TKWeekViewModel = viewModel()) {
    MaterialTheme(
        colorScheme = colorScheme()
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val threePaneScaffoldNavigator =
            rememberListDetailPaneScaffoldNavigator<TKWeekModule>(
                scaffoldDirective = calculatePaneScaffoldDirective(
                    windowAdaptiveInfo = currentWindowAdaptiveInfo(),
                    verticalHingePolicy = if (uiState.avoidHinge) HingePolicy.AlwaysAvoid else HingePolicy.NeverAvoid
                )
            )
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val scope = rememberCoroutineScope()
        val appBarActions by viewModel.appBarActions.collectAsState()
        val listVisible =
            threePaneScaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
        val detailVisible =
            threePaneScaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
        var activeModuleTitleRes by remember(uiState.topLevelModuleWithArguments) {
            mutableIntStateOf(
                uiState.topLevelModuleWithArguments.module.titleRes
            )
        }
        LaunchedEffect(currentBackStackEntry) {
            currentBackStackEntry?.destination?.route?.let { route ->
                TKWeekModule.entries.firstOrNull { it.name == route }?.let {
                    activeModuleTitleRes = it.titleRes
                }
            }
        }
        LaunchedEffect(listVisible) {
            if (!listVisible) {
                viewModel.setListScrolled(false)
            }
        }
        LaunchedEffect(Unit) {
            viewModel.navigationTrigger.collect { navigationEvent ->
                if (navigationEvent.topLevel) {
                    threePaneScaffoldNavigator.navigateTo(
                        ListDetailPaneScaffoldRole.Detail
                    )
                } else {
                    with(navigationEvent.moduleWithArguments) {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            ARGUMENTS,
                            arguments
                        )
                        navController.navigate(module.name)
                    }
                }
            }
        }
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
                val hasStackedModules =
                    currentBackStackEntry != null && navController.previousBackStackEntry != null
                val canNavigateBack =
                    threePaneScaffoldNavigator.canNavigateBack() || hasStackedModules
                val onNavigateBack: () -> Unit = {
                    if (hasStackedModules) {
                        navController.popBackStack()
                    } else {
                        scope.launch {
                            threePaneScaffoldNavigator.navigateBack()
                        }
                    }
                }
                TKWeekTopAppBar(
                    uiState = uiState,
                    detailVisible = detailVisible,
                    activeModuleTitleRes = activeModuleTitleRes,
                    appBarActions = appBarActions,
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            NavigableListDetailPaneScaffold(
                navigator = threePaneScaffoldNavigator,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = horizontalPadding),
                listPane = {
                    TKWeekModuleSelector(
                        currentRoute = currentBackStackEntry?.destination?.route ?: "",
                        onModuleSelected = { module ->
                            viewModel.selectModuleWithArguments(
                                module = module,
                                arguments = null,
                                topLevel = true
                            )
                        },
                        detailVisible = detailVisible,
                        onListStateChanged = { isAtTop ->
                            viewModel.setListScrolled(!isAtTop)
                        }
                    )
                },
                detailPane = {
                    TKWeekDetailPane(
                        uiState = uiState,
                        navController = navController,
                        detailVisible = detailVisible,
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TKWeekAppPreview() {
    TKWeekApp()
}
