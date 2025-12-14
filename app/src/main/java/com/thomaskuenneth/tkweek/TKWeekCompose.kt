package com.thomaskuenneth.tkweek

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
        val threePaneScaffoldNavigator = rememberListDetailPaneScaffoldNavigator<TKWeekModule>()
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val scope = rememberCoroutineScope()
        val uiState by viewModel.uiState.collectAsState()
        val appBarActions by viewModel.appBarActions.collectAsState()
        val listVisible =
            threePaneScaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
        val detailVisible =
            threePaneScaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        var isListAtTop by remember { mutableStateOf(true) }
        val topAppBarState = scrollBehavior.state
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
        LaunchedEffect(Unit) {
            viewModel.fragmentScrollDelta.collect {
                topAppBarState.contentOffset -= it
            }
        }
        LaunchedEffect(Unit) {
            viewModel.resetScrollTrigger.collect {
                if (isListAtTop) {
                    topAppBarState.contentOffset = 0f
                }
            }
        }
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets(),
            topBar = {
                if (uiState.showSearchBar) {
                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
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
                        active = false,
                        onActiveChange = {
                            if (it) {
                                viewModel.setSearchActive(true)
                            }
                        },
                        placeholder = { Text(stringResource(id = R.string.search_hint)) },
                        leadingIcon = {
                            if (uiState.isSearchActive) {
                                IconButton(onClick = {
                                    viewModel.setSearchActive(false)
                                    viewModel.setSearchQuery("")
                                    focusManager.clearFocus()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            } else {
                                val hasStackedModules =
                                    currentBackStackEntry != null && navController.previousBackStackEntry != null
                                if (threePaneScaffoldNavigator.canNavigateBack() || hasStackedModules) {
                                    IconButton(
                                        onClick = {
                                            if (hasStackedModules) {
                                                navController.popBackStack()
                                            } else {
                                                scope.launch {
                                                    threePaneScaffoldNavigator.navigateBack()
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                            contentDescription = null,
                                        )
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
                                IconButton(onClick = {
                                    viewModel.setSearchQuery("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    ) {
                    }
                } else {
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
                                IconButton(
                                    onClick = {
                                        if (hasStackedModules) {
                                            navController.popBackStack()
                                        } else {
                                            scope.launch {
                                                threePaneScaffoldNavigator.navigateBack()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                        actions = {
                            if (detailVisible) {
                                TKWeekAppBarActions(appBarActions)
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
            }
        ) { paddingValues ->
            val displayCutoutInsets = WindowInsets.displayCutout
            val density = LocalDensity.current
            val layoutDirection = LocalLayoutDirection.current
            val left = displayCutoutInsets.getLeft(density, layoutDirection)
            val right = displayCutoutInsets.getRight(density, layoutDirection)
            val horizontalPadding =
                with(density) { max(left, right).toDp() }.coerceAtLeast(16.dp)
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
                        onListStateChanged = { isListAtTop = it }
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
                                            arguments = args,
                                        ) { viewModel.resetScroll() }
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
