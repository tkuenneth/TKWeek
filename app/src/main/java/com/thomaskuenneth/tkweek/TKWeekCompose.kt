package com.thomaskuenneth.tkweek

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thomaskuenneth.tkweek.ui.BackArrow
import com.thomaskuenneth.tkweek.ui.ClearIcon
import com.thomaskuenneth.tkweek.ui.TKWeekAppBarActions
import com.thomaskuenneth.tkweek.ui.TKWeekModuleContainer
import com.thomaskuenneth.tkweek.ui.TKWeekModuleSelector
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
        val focusManager = LocalFocusManager.current
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
                if (uiState.showSearchBar) {
                    SearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = uiState.searchQuery,
                                onQueryChange = {
                                    viewModel.setSearchQuery(it)
                                    if (!uiState.isSearchActive) {
                                        viewModel.setSearchActive(true)
                                    }
                                },
                                onSearch = {
                                    focusManager.clearFocus()
                                },
                                expanded = uiState.isSearchActive,
                                onExpandedChange = { viewModel.setSearchActive(it) },
                                placeholder = { Text(stringResource(id = R.string.events)) },
                                leadingIcon = {
                                    if (uiState.isSearchActive) {
                                        BackArrow(
                                            onClick = {
                                                viewModel.setSearchActive(false)
                                                viewModel.setSearchQuery("")
                                                focusManager.clearFocus()
                                            },
                                            description = R.string.close_search
                                        )
                                    } else {
                                        val hasStackedModules =
                                            currentBackStackEntry != null && navController.previousBackStackEntry != null
                                        if (threePaneScaffoldNavigator.canNavigateBack() || hasStackedModules) {
                                            BackArrow {
                                                if (hasStackedModules) {
                                                    navController.popBackStack()
                                                } else {
                                                    scope.launch {
                                                        threePaneScaffoldNavigator.navigateBack()
                                                    }
                                                }
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        ClearIcon {
                                            viewModel.setSearchQuery("")
                                        }
                                    }
                                }
                            )
                        },
                        expanded = false,
                        onExpandedChange = {
                            if (it) {
                                viewModel.setSearchActive(true)
                            }
                        },
                    ) {
                    }
                } else {
                    val isScrolled = uiState.isListScrolled || uiState.isDetailScrolled
                    val topAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    val containerColor by animateColorAsState(
                        targetValue = if (isScrolled)
                            topAppBarColors.scrolledContainerColor
                        else
                            topAppBarColors.containerColor,
                        label = "containerColor"
                    )
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(
                                    if (listVisible) {
                                        R.string.app_name
                                    } else {
                                        activeModuleTitleRes
                                    }
                                )
                            )
                        },
                        navigationIcon = {
                            val hasStackedModules =
                                currentBackStackEntry != null && navController.previousBackStackEntry != null
                            if (threePaneScaffoldNavigator.canNavigateBack() || hasStackedModules) {
                                BackArrow {
                                    if (hasStackedModules) {
                                        navController.popBackStack()
                                    } else {
                                        scope.launch {
                                            threePaneScaffoldNavigator.navigateBack()
                                        }
                                    }
                                }
                            }
                        },
                        actions = {
                            if (detailVisible) {
                                TKWeekAppBarActions(appBarActions)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = containerColor
                        )
                    )
                }
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
                    AnimatedPane {
                        val startDestination = uiState.topLevelModuleWithArguments.module.name
                        key(startDestination) {
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
                                        })
                                    ) {
                                        val args =
                                            navController.previousBackStackEntry?.savedStateHandle?.get<Bundle>(
                                                ARGUMENTS
                                            )

                                        TKWeekModuleContainer(
                                            module = moduleEntry,
                                            arguments = args
                                        )
                                    }
                                 }
                            }
                        }
                    }
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
