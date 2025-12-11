package com.thomaskuenneth.tkweek.activity

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
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
import androidx.compose.runtime.key
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
import com.thomaskuenneth.tkweek.fragment.AboutFragment
import com.thomaskuenneth.tkweek.fragment.AboutYearFragment
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment
import com.thomaskuenneth.tkweek.fragment.CalendarFragment
import com.thomaskuenneth.tkweek.fragment.DateCalculatorFragment
import com.thomaskuenneth.tkweek.fragment.DaysBetweenDatesFragment
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.fragment.WeekFragment
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class TKWeekCompose : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TKWeekApp()
        }
    }
}

@Parcelize
data class Module(
    @get:StringRes val titleRes: Int,
    @get:StringRes val descriptionRes: Int,
    val clazz: Class<*>
) : Parcelable

private val modules = listOf(
    Module(
        R.string.week_activity_text1,
        R.string.week_activity_text2,
        WeekFragment::class.java
    ),
    Module(
        R.string.myday_activity_text1,
        R.string.myday_activity_text2,
        MyDayFragment::class.java
    ),
    Module(
        R.string.days_between_dates_activity_text1,
        R.string.days_between_dates_activity_text2,
        DaysBetweenDatesFragment::class.java
    ),
    Module(
        R.string.date_calculator_activity_text1,
        R.string.date_calculator_activity_text2,
        DateCalculatorFragment::class.java
    ),
    Module(
        R.string.annual_events_activity_text1,
        R.string.annual_events_activity_text2,
        AnnualEventsFragment::class.java
    ),
    Module(
        R.string.about_a_year_activity_text1,
        R.string.about_a_year_activity_text2,
        AboutYearFragment::class.java
    ),
    Module(
        R.string.calendar_activity_text1,
        R.string.calendar_activity_text2,
        CalendarFragment::class.java
    ),
    Module(
        R.string.about_activity_text1,
        R.string.about_activity_text2,
        AboutFragment::class.java
    )
)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun TKWeekApp(modifier: Modifier = Modifier) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Module>()
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
        var selectedModule by rememberSaveable { mutableStateOf<Module?>(null) }
        ListDetailPaneScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                LazyColumn {
                    items(modules) { module ->
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
                key(selectedModule) {
                    selectedModule?.let { module ->
                        FragmentContainer(
                            modifier = Modifier.fillMaxSize(),
                            fragmentClass = module.clazz
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun FragmentContainer(
    modifier: Modifier = Modifier,
    fragmentClass: Class<*>
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FragmentContainerView(context).apply {
                id = R.id.fragment_container_view
            }
        },
        update = { view ->
            val fragmentManager = (view.context as AppCompatActivity).supportFragmentManager
            val fragment =
                fragmentClass.getConstructor().newInstance() as androidx.fragment.app.Fragment
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
