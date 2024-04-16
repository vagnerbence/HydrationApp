package hu.app.wearablehydration.service

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService


class SimpleMessageListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if ("/simple_message_path" == item.uri.path) {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val message = dataMap.getString("simple_message_key")
                    Log.d("SimpleMessageListener", "Message received on watch: $message")

                    // Itt kezelheted az üzenetet
                }
            }
        }
    }
}