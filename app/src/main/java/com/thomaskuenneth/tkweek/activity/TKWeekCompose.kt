package com.thomaskuenneth.tkweek.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.thomaskuenneth.tkweek.R
import kotlinx.coroutines.launch

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
        var selectedModule by rememberSaveable { mutableStateOf(TKWeekModule.Week) }
        ListDetailPaneScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                LazyColumn {
                    items(TKWeekModule.entries) { module ->
                        ListItem(
                            headlineContent = { Text(text = stringResource(id = module.titleRes)) },
                            supportingContent = { Text(text = stringResource(id = module.descriptionRes)) },
                            modifier = Modifier.clickable {
                                selectedModule = module
                                scope.launch {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = selectedModule
                                    )
                                }
                            }
                        )
                    }
                }
            },
            detailPane = {
                FragmentContainer(
                    module = selectedModule
                )
            }
        )
    }
}

@Composable
fun FragmentContainer(
    module: TKWeekModule
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
                module.clazz.getConstructor()
                    .newInstance() as androidx.fragment.app.Fragment
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
