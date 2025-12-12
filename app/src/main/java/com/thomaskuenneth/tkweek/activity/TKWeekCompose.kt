package com.thomaskuenneth.tkweek.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.types.FragmentInfo
import com.thomaskuenneth.tkweek.types.FragmentInfoSaver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.max

@AndroidEntryPoint
class TKWeekCompose : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TKWeekApp()
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun TKWeekApp() {
    val navigator = rememberListDetailPaneScaffoldNavigator<TKWeekModule>()
    val scope = rememberCoroutineScope()
    Scaffold(
        contentWindowInsets = WindowInsets(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    if (navigator.canNavigateBack()) {
                        IconButton(onClick = {
                            scope.launch {
                                navigator.navigateBack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val displayCutoutInsets = WindowInsets.displayCutout
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val left = displayCutoutInsets.getLeft(density, layoutDirection)
        val right = displayCutoutInsets.getRight(density, layoutDirection)
        val horizontalPadding = with(density) { max(left, right).toDp() }.coerceAtLeast(16.dp)
        var selectedModule by rememberSaveable(stateSaver = FragmentInfoSaver) {
            mutableStateOf(
                FragmentInfo(module = TKWeekModule.Week, arguments = null)
            )
        }
        val detailVisible =
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
        NavigableListDetailPaneScaffold(
            navigator = navigator,
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = horizontalPadding),
            listPane = {
                TKWeekModuleSelector(
                    selectedModule = selectedModule.module,
                    onModuleSelected = { module ->
                        selectedModule = FragmentInfo(module = module, arguments = null)
                        scope.launch {
                            navigator.navigateTo(
                                pane = ListDetailPaneScaffoldRole.Detail,
                                contentKey = selectedModule.module
                            )
                        }
                    },
                    detailVisible = detailVisible
                )
            },
            detailPane = {
                FragmentContainer(
                    fragmentInfo = selectedModule
                )
            }
        )
    }
}

@Composable
fun FragmentContainer(
    fragmentInfo: FragmentInfo
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            FragmentContainerView(context).apply {
                id = R.id.fragment_container_view
            }
        },
        update = { view ->
            val fragmentManager = (view.context as AppCompatActivity).supportFragmentManager
            val fragment =
                fragmentInfo.module.clazz.getConstructor()
                    .newInstance() as androidx.fragment.app.Fragment
            fragment.arguments = fragmentInfo.arguments
            fragmentManager.beginTransaction()
                .replace(view.id, fragment)
                .commit()
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TKWeekAppPreview() {
    TKWeekApp()
}
