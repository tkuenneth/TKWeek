package com.thomaskuenneth.tkweek.types

import android.os.Bundle
import androidx.compose.runtime.saveable.listSaver
import com.thomaskuenneth.tkweek.activity.TKWeekModule

data class FragmentInfo(val module: TKWeekModule, val bundle: Bundle? = null)

val FragmentInfoSaver = listSaver<FragmentInfo, Any?>(
    save = { listOf(it.module.name, it.bundle) },
    restore = {
        val moduleName = it[0] as String
        val bundle = it[1] as Bundle?
        FragmentInfo(TKWeekModule.valueOf(moduleName), bundle)
    }
)
