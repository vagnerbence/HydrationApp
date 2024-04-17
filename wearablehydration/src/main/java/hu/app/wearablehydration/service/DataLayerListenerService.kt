package hu.app.wearablehydration.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DataLayerListenerService : WearableListenerService() {

    companion object {
        const val ACTION_UPDATE_WATER_INTAKE = "ACTION_UPDATE_WATER_INTAKE"
        const val EXTRA_CURRENT_INTAKE = "EXTRA_CURRENT_INTAKE"
        const val EXTRA_TOTAL_INTAKE = "EXTRA_TOTAL_INTAKE" // Új konstans a teljes vízfogyasztás fogadására
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val path = event.dataItem.uri.path
                if ("/user_data" == path) { // Ellenőrizzük, hogy a megfelelő adatokat fogadjuk-e
                    val currentIntake = dataMapItem.dataMap.getFloat("currentWaterIntake") // Jelenlegi vízfogyasztás fogadása
                    val totalIntake = dataMapItem.dataMap.getDouble("totalWaterIntake").toFloat() // Teljes vízfogyasztás fogadása és Float-té konvertálása

                    Log.d("DataLayerListenerService", "Received current water intake: $currentIntake, total intake: $totalIntake")

                    // Lokális broadcast küldése az új vízfogyasztási értékekkel
                    val updateIntent = Intent(ACTION_UPDATE_WATER_INTAKE)
                    updateIntent.putExtra(EXTRA_CURRENT_INTAKE, currentIntake)
                    updateIntent.putExtra(EXTRA_TOTAL_INTAKE, totalIntake)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent)
                }
            }
        }
    }
}
