package xyz.malkki.neostumbler.ui.composables.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import xyz.malkki.neostumbler.PREFERENCES
import xyz.malkki.neostumbler.R
import xyz.malkki.neostumbler.constants.PreferenceKeys
import xyz.malkki.neostumbler.extensions.get
import xyz.malkki.neostumbler.scanner.movement.MovementDetectorType

private val TITLES =
    mapOf(
        MovementDetectorType.NONE to R.string.movement_detection_none_title,
        MovementDetectorType.LOCATION to R.string.movement_detection_location_title,
        MovementDetectorType.SIGNIFICANT_MOTION to
            R.string.movement_detection_significant_motion_title,
    )

private val DESCRIPTIONS =
    mapOf(
        MovementDetectorType.NONE to R.string.movement_detection_none_description,
        MovementDetectorType.LOCATION to R.string.movement_detection_location_description,
        MovementDetectorType.SIGNIFICANT_MOTION to
            R.string.movement_detection_significant_motion_description,
    )

private fun DataStore<Preferences>.movementDetector(): Flow<MovementDetectorType> =
    data
        .map { preferences ->
            preferences.get<MovementDetectorType>(PreferenceKeys.MOVEMENT_DETECTOR)
                ?: MovementDetectorType.LOCATION
        }
        .distinctUntilChanged()

@Composable
fun MovementDetectorSettings() {
    val context = LocalContext.current

    val settingsStore = koinInject<DataStore<Preferences>>(PREFERENCES)

    val movementDetectorType = settingsStore.movementDetector().collectAsState(initial = null)

    if (movementDetectorType.value != null) {
        MultiChoiceSettings(
            title = stringResource(id = R.string.movement_detection),
            options = MovementDetectorType.entries,
            selectedOption = movementDetectorType.value!!,
            disabledOptions =
                if (context.significantMotionSensorAvailable()) {
                    emptySet()
                } else {
                    setOf(MovementDetectorType.SIGNIFICANT_MOTION)
                },
            titleProvider = { ContextCompat.getString(context, TITLES[it]!!) },
            descriptionProvider = { ContextCompat.getString(context, DESCRIPTIONS[it]!!) },
            onValueSelected = { newMovementDetectorType ->
                settingsStore.updateData { prefs ->
                    prefs.toMutablePreferences().apply {
                        set(
                            stringPreferencesKey(PreferenceKeys.MOVEMENT_DETECTOR),
                            newMovementDetectorType.name,
                        )
                    }
                }
            },
        )
    }
}

private fun Context.significantMotionSensorAvailable(): Boolean {
    return getSystemService<SensorManager>()!!.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION) !=
        null
}
