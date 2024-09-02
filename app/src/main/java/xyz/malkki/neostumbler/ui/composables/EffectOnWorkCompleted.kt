package xyz.malkki.neostumbler.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import java.util.UUID

/**
 * Helper composable for running an effect when work is completed
 *
 * @param Work ID for [WorkManager]
 * @param onWorkSuccess Callback to be called when the work is finished successfully
 * @param onWorkFailed Callback to be called when the work is finished with a failure
 */
@Composable
fun EffectOnWorkCompleted(workId: UUID?, onWorkSuccess: (WorkInfo) -> Unit, onWorkFailed: (WorkInfo) -> Unit) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    
    LaunchedEffect(workId) {
        if (workId != null) {
            workManager.getWorkInfoByIdFlow(workId)
                .filterNotNull()
                .filter { it.state == WorkInfo.State.SUCCEEDED || it.state == WorkInfo.State.FAILED }
                .collectLatest {
                    if (it.state == WorkInfo.State.SUCCEEDED) {
                        onWorkSuccess(it)
                    } else if (it.state == WorkInfo.State.FAILED) {
                        onWorkFailed(it)
                    }
                }
        }
    }
}