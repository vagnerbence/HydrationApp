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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.Wearable
import hu.app.wearablehydration.presentation.theme.HydrationAppTheme
import hu.app.wearablehydration.service.DataLayerListenerService
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    private var currentWaterIntake: Float = 0f
    private var totalWaterIntake: Float = 2.0f //alapértelmezett érték,ezt kell frissíteni

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DataLayerListenerService.ACTION_UPDATE_WATER_INTAKE) {
                currentWaterIntake = intent.getFloatExtra(DataLayerListenerService.EXTRA_CURRENT_INTAKE, 0f)
                totalWaterIntake = intent.getFloatExtra(DataLayerListenerService.EXTRA_TOTAL_INTAKE, 2.0f)
                refreshUI() //UI frissítése az új adatokkal
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

    private fun refreshUI() {
        setContent {
            HydrationAppTheme {
                val decimalFormat = DecimalFormat("#.0")
                val formattedCurrentWaterIntake = decimalFormat.format(currentWaterIntake)
                val formattedTotalWaterIntake = decimalFormat.format(totalWaterIntake)
                val intakePercentage = if (totalWaterIntake > 0) {
                    (currentWaterIntake / totalWaterIntake * 100).toInt() //százalék számítás
                } else {
                    0
                }

                Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Current Water Intake: $formattedCurrentWaterIntake L")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Total Water Intake: $formattedTotalWaterIntake L")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Intake Percentage: $intakePercentage%")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        val amounts = listOf(0.1f, 0.2f, 0.5f, 1f)
                        amounts.forEach { amount ->
                            Button(
                                    modifier = Modifier
                                            .size(40.dp, 40.dp)
                                            .padding(1.dp),
                                    onClick = { sendWaterIntakeUpdateToPhone(amount) }) {
                                Text("$amount L", style = MaterialTheme.typography.bodyLarge)

                            }
                        }
                    }
                }
            }
        }
    }


    private fun sendWaterIntakeUpdateToPhone(amount: Float) {
        val path = "/update_water_intake"
        val message = amount.toString().toByteArray()
        Wearable.getMessageClient(this).sendMessage("path_to_phone", path, message).addOnSuccessListener {
            Log.d("WearableApp", "Message sent successfully $amount")
        }.addOnFailureListener {
            Log.e("WearableApp", "Message send failed")
        }
    }
}
