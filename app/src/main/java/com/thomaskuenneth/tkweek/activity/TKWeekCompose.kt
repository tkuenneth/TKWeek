package com.thomaskuenneth.tkweek.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.fragment.AboutFragment
import com.thomaskuenneth.tkweek.fragment.AboutYearFragment
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment
import com.thomaskuenneth.tkweek.fragment.CalendarFragment
import com.thomaskuenneth.tkweek.fragment.DateCalculatorFragment
import com.thomaskuenneth.tkweek.fragment.DaysBetweenDatesFragment
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.fragment.WeekFragment
import com.thomaskuenneth.tkweek.ui.theme.TKWeekTheme

class TKWeekCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TKWeekTheme {
                TKWeekApp()
            }
        }
    }
}

data class Module(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val clazz: Class<*>
)

private val modules = listOf(
    Module(R.string.week_activity_text1, R.string.week_activity_text2, Icons.Default.Star, WeekFragment::class.java),
    Module(R.string.myday_activity_text1, R.string.myday_activity_text2, Icons.Default.Star, MyDayFragment::class.java),
    Module(R.string.days_between_dates_activity_text1, R.string.days_between_dates_activity_text2, Icons.Default.Star, DaysBetweenDatesFragment::class.java),
    Module(R.string.date_calculator_activity_text1, R.string.date_calculator_activity_text2, Icons.Default.Star, DateCalculatorFragment::class.java),
    Module(R.string.annual_events_activity_text1, R.string.annual_events_activity_text2, Icons.Default.Star, AnnualEventsFragment::class.java),
    Module(R.string.about_a_year_activity_text1, R.string.about_a_year_activity_text2, Icons.Default.Star, AboutYearFragment::class.java),
    Module(R.string.calendar_activity_text1, R.string.calendar_activity_text2, Icons.Default.Star, CalendarFragment::class.java),
    Module(R.string.about_activity_text1, R.string.about_activity_text2, Icons.Default.Star, AboutFragment::class.java)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TKWeekApp(modifier: Modifier = Modifier) {
    var selectedModule by remember { mutableStateOf<Module?>(null) }
    val context = LocalContext.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                modules.forEach { module ->
                    item(
                        selected = selectedModule == module,
                        onClick = { selectedModule = module },
                        icon = { Icon(imageVector = module.icon, contentDescription = null) },
                        label = { Text(text = stringResource(id = module.titleRes)) }
                    )
                }
            }
        ) {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize()
            ) {
                items(modules) { module ->
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = module.titleRes)) },
                        supportingContent = { Text(text = stringResource(id = module.descriptionRes)) },
                        modifier = Modifier.clickable {
                            val intent = Intent(context, ModuleContainerActivity::class.java)
                            intent.putExtra("clazz", module.clazz)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TKWeekAppPreview() {
    TKWeekTheme {
        TKWeekApp()
    }
}
