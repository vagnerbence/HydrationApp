package hu.app.hydrationapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import hu.app.hydrationapp.model.DailyWaterIntake;

public class ResetWaterIntakeService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ResetService", "Service started to reset water intake");
        resetUserWaterIntake();
        return START_NOT_STICKY;
    }

    private void resetUserWaterIntake() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            DailyWaterIntake dailyIntake = new DailyWaterIntake(today, false);

            Map<String, Object> updates = new HashMap<>();
            updates.put("currentWaterIntake", 0f);
            updates.put("dailyWaterIntakes/" + today, dailyIntake);

            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("ResetService", "Service started to reset water intake");
                } else {
                    Log.d("ResetService", "ERROR ERROR ERROR");
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}