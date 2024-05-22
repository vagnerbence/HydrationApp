package hu.app.hydrationapp.ui.home;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import hu.app.hydrationapp.databinding.FragmentHomeBinding;
import hu.app.hydrationapp.model.DailyWaterIntake;
import hu.app.hydrationapp.model.ReminderBroadcastReceiver;
import hu.app.hydrationapp.model.User;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadUserData();

        setupButtons();


        Wearable.getMessageClient(getContext()).addListener(messageReceivedListener);


        return root;
    }


    private final MessageClient.OnMessageReceivedListener messageReceivedListener = new MessageClient.OnMessageReceivedListener() {
        @Override
        public void onMessageReceived(@NonNull MessageEvent messageEvent) {
            if ("/update_water_intake".equals(messageEvent.getPath())) {
                final String amountString = new String(messageEvent.getData(), StandardCharsets.UTF_8);
                final float amount = Float.parseFloat(amountString);
                Log.d(TAG, "addWater called with amount: " + amount);
                getActivity().runOnUiThread(() -> addWater(amount));
            }
        }
    };

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null) {

                        updateUIBasedOnUser(user);

                        resetCurrentWaterIntakeIfNeeded(user);

                        sendUserDataToWearable(user); //wearable-ra küldés

                        //kezdő animáció állapot és sikeres animáció elrejtése új felhasználók esetén
                        float initialProgress = user.getCurrentWaterIntake() / (float) user.getTotalWaterIntake();
                        binding.animationView.setProgress(initialProgress);
                        binding.successAnimation.setVisibility(View.GONE);
                    } else {

                        handleNoUserData();

                    }
                }
            });
        }
    }

    private void updateUIBasedOnUser(User user) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedTotalValue = decimalFormat.format(user.getTotalWaterIntake());
        String formattedCurrentValue = decimalFormat.format(user.getCurrentWaterIntake());
        binding.quantityEditText.setText(formattedTotalValue);
        binding.currentEditText.setText(formattedCurrentValue);
    }

    private void resetCurrentWaterIntakeIfNeeded(User user) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (!today.equals(user.getLastUpdateDate())) {
            user.setCurrentWaterIntake(0);
            user.setLastUpdateDate(today);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                mDatabase.child("users").child(uid).setValue(user);
            }
        }
    }


    private void handleNoUserData(){
        binding.quantityEditText.setText("0");
        binding.currentEditText.setText("0");
        binding.animationView.setProgress(0f);
        binding.successAnimation.setVisibility(View.GONE);
    }


    private void setupButtons() {

        ImageButton btnFivePercent = binding.btnFivePercent;
        btnFivePercent.setOnClickListener(v -> addWater(0.1f));

        ImageButton btnTenPercent = binding.btnTenPercent;
        btnTenPercent.setOnClickListener(v -> addWater(0.2f));

        ImageButton btnTwentyFivePercent = binding.btnTwentyFivePercent;
        btnTwentyFivePercent.setOnClickListener(v -> addWater(0.5f));

        ImageButton btnFiftyPercent = binding.btnFiftyPercent;
        btnFiftyPercent.setOnClickListener(v -> addWater(1.0f));
    }

    private void addWater(float amount) {
        //felhasználó lekérdezése
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //van-e bejelentkezett felhasználó
        if (currentUser != null) {
            //egyedi azonosítójának lekérdezése
            sendSimpleMessageToWear();
            String uid = currentUser.getUid();
            // Firebase-ből a felhasználó adatainak lekérdezése
            mDatabase.child("users").child(uid).get().addOnCompleteListener(task -> {
                // lekérdezés sikeres volt-e és van-e adat
                if (task.isSuccessful() && task.getResult().getValue(User.class) != null) {
                    //adatok konvertálása User objektummá
                    User user = task.getResult().getValue(User.class);
                    //vízfogyasztás kiszámítása, de nem haladhatja meg a teljes napi bevitelt
                    float newCurrentWaterIntake = Math.min(user.getCurrentWaterIntake() + amount, (float) user.getTotalWaterIntake());
                    //frissítjük a felhasználó jelenlegi vízfogyasztását az új értékkel
                    user.setCurrentWaterIntake(newCurrentWaterIntake);



                    //adott napra vonatkozó vízfogyasztási adatok frissítése vagy létrehozása a DailyWaterIntake objektummal
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    boolean goalAchieved = (newCurrentWaterIntake + 0.01f) >= user.getTotalWaterIntake();
                    DailyWaterIntake dailyIntake = new DailyWaterIntake(today, goalAchieved);
                    if (user.getDailyWaterIntakes() == null) {
                        user.setDailyWaterIntakes(new HashMap<>());
                    }
                    user.getDailyWaterIntakes().put(today, dailyIntake);
                    scheduleReminderIfGoalNotAchieved(user, goalAchieved);

                    sendUserDataToWearable(user);


                    //felhasználó adatbázisban történő frissítése az új adatokkal
                    mDatabase.child("users").child(uid).setValue(user).addOnCompleteListener(updateTask -> {
                        //ellenőrizzük, hogy a frissítés sikeres volt-e
                        if (updateTask.isSuccessful()) {
                            //frissítjük a UI-t az új vízfogyasztási adatokkal
                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            String formattedValue = decimalFormat.format(newCurrentWaterIntake);
                            binding.currentEditText.setText(formattedValue);

                            //sendWaterIntakeUpdateToWearable(user.getCurrentWaterIntake());
                            sendUserDataToWearable(user);

                            //animáció frissítése az új vízfogyasztási adatok alapján
                            float progress = newCurrentWaterIntake / (float) user.getTotalWaterIntake();
                            if (binding.animationView.getProgress() < progress) {
                                // Csak akkor játsszuk le az animációt, ha a progress növekedett
                                binding.animationView.setMinAndMaxProgress(binding.animationView.getProgress(), progress);
                                binding.animationView.playAnimation();
                            }

                            //felhasználó elérte a napi vízfogyasztási célját, lejátszuk a siker animációt
                            if (goalAchieved) {
                                playSuccessAnimation();
                            }
                        }
                    });
                }
            });
        }
    }


    private void playSuccessAnimation() {
        LottieAnimationView successAnimation = binding.successAnimation;
        successAnimation.setVisibility(View.VISIBLE); // láthatóvá tétel
        successAnimation.setAnimation("success2.json");
        successAnimation.playAnimation();
    }

    private void scheduleReminderIfGoalNotAchieved(User user, boolean goalAchieved) {
        if (!goalAchieved) {
            //értesítési csatorna létrehozása
            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel("hydrateReminder") == null) {
                NotificationChannel channel = new NotificationChannel("hydrateReminder", "Hydration Reminder", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            //alarmManager használata az értesítés beállításához
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getContext(), ReminderBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            //időzítést itt, például minden két órában, csak a napi aktív időszakban
            Calendar startTime = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, 8);
            startTime.set(Calendar.MINUTE, 0);
            long intervalTime = 1000 * 60* 60* 2; // 2 óránként
           //long intervalTime = 1000 * 30; // fél percenként videó miatt

            Calendar endTime = Calendar.getInstance();
            endTime.set(Calendar.HOUR_OF_DAY, 18);
            endTime.set(Calendar.MINUTE, 0);
            endTime.set(Calendar.SECOND, 0);



            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(), intervalTime, pendingIntent);
        }
    }





    private void sendUserDataToWearable(User user) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/user_data");
        dataMap.getDataMap().putString("username", user.getName());
        dataMap.getDataMap().putFloat("currentWaterIntake", user.getCurrentWaterIntake());
        dataMap.getDataMap().putDouble("totalWaterIntake", user.getTotalWaterIntake());
        dataMap.getDataMap().putLong("timestamp", new Date().getTime()); // Az időbélyeg biztosítja, hogy az adatfrissítés észlelhető legyen.
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Wearable.getDataClient(getActivity()).putDataItem(request)
                .addOnSuccessListener(dataItem -> Log.d(TAG, "Data sent successfully: " + dataItem))
                .addOnFailureListener(e -> Log.e(TAG, "Data send failed", e));
    }



    private void sendSimpleMessageToWear() {
        //létrehozzuk az adatküldési kérést csak próba, küldés - fogadás megvalósítása
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/simple_message_path");
        putDataMapReq.getDataMap().putString("simple_message_key", "Víz hozzáadva");
        putDataMapReq.getDataMap().putLong("timestamp", new Date().getTime()); // Időbélyeg hozzáadása

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();

        Task<DataItem> putDataTask = Wearable.getDataClient(getActivity()).putDataItem(putDataReq);
        putDataTask.addOnSuccessListener(dataItem -> {
            Log.d(TAG, "Egyszerű üzenet sikeresen elküldve az órára.");
        }).addOnFailureListener(e -> Log.e(TAG, "Egyszerű üzenet küldése sikertelen.", e));
    }


    @Override
    public void onStop() {
        super.onStop();
        Wearable.getMessageClient(getContext()).removeListener(messageReceivedListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}