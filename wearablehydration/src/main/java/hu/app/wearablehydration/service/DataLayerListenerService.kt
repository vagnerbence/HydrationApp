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
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val path = event.dataItem.uri.path
                if ("/update_water_intake" == path) {
                    val currentIntake = dataMapItem.dataMap.getFloat("currentIntake") // Itt használd a "currentIntake" kulcsot
                    Log.d("DataLayerListenerService", "Received water intake update: $currentIntake")

                    // Lokális broadcast küldése az új vízfogyasztási értékkel
                    val updateIntent = Intent(ACTION_UPDATE_WATER_INTAKE)
                    updateIntent.putExtra(EXTRA_CURRENT_INTAKE, currentIntake) // Itt is "currentIntake" kulcsot használj
                    LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent)
                }
            }
        }
    }
}