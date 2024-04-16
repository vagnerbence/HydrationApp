package hu.app.wearablehydration.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.Wearable
import hu.app.wearablehydration.service.DataLayerListenerService
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    private var currentWaterIntake: Float = 0f

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DataLayerListenerService.ACTION_UPDATE_WATER_INTAKE) {
                // Használd az EXTRA_AMOUNT kulcsot, ha az adatok fogadásához azt használod a küldő oldalon
                val newWaterIntake = intent.getFloatExtra(DataLayerListenerService.EXTRA_CURRENT_INTAKE, 0f)

                runOnUiThread {
                    currentWaterIntake = newWaterIntake
                    Log.d("MainActivity", "Received water intake update: $currentWaterIntake L")
                    // UI frissítése, ha szükséges. Ebben az esetben a setContent újrameghívása frissíti az UI-t
                    refreshUI()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, IntentFilter(DataLayerListenerService.ACTION_UPDATE_WATER_INTAKE))
        refreshUI()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver)
        super.onDestroy()
    }

    private fun sendWaterIntakeUpdateToPhone(amount: Float) {
        val path = "/update_water_intake"
        val message = amount.toString().toByteArray()
        Wearable.getMessageClient(this).sendMessage("path_to_phone", path, message).addOnSuccessListener {
            Log.d("WearableApp", "Message sent successfully: $amount")
        }.addOnFailureListener {
            Log.e("WearableApp", "Message send failed")
        }
    }

    private fun refreshUI() {
        setContent {
            val decimalFormat = DecimalFormat("#.0") // Formátum definiálása egy tizedesjegyre
            val formattedWaterIntake = decimalFormat.format(currentWaterIntake) // Az érték formázása

            MaterialTheme {
                Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Current Water Intake: $formattedWaterIntake L")
                    Button(onClick = { sendWaterIntakeUpdateToPhone(0.1f) }) {
                        Text("1 dl")
                    }
                }
            }
        }
    }
}
