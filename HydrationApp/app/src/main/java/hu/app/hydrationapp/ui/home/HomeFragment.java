package hu.app.hydrationapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import hu.app.hydrationapp.databinding.FragmentHomeBinding;
import hu.app.hydrationapp.model.DailyWaterIntake;
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

        return root;
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null) {
                        DecimalFormat decimalFormat = new DecimalFormat("#.##");
                        String formattedTotalValue = decimalFormat.format(user.getTotalWaterIntake());
                        String formattedCurrentValue = decimalFormat.format(user.getCurrentWaterIntake());
                        binding.quantityEditText.setText(formattedTotalValue);
                        binding.currentEditText.setText(formattedCurrentValue);

                        // Kezdő animáció állapot és sikeres animáció elrejtése új felhasználók esetén
                        float initialProgress = user.getCurrentWaterIntake() / (float) user.getTotalWaterIntake();
                        binding.animationView.setProgress(initialProgress);
                        binding.successAnimation.setVisibility(View.GONE);
                    } else {
                        // Kezelje az esetet, ha az adatok nem érhetők el (pl. új felhasználó)
                        binding.quantityEditText.setText("0");
                        binding.currentEditText.setText("0");
                        binding.animationView.setProgress(0f);
                        binding.successAnimation.setVisibility(View.GONE);
                    }
                }
            });
        }
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
            String uid = currentUser.getUid();
            // Adatbázisból a felhasználó adatainak lekérdezése
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
                    boolean goalAchieved = newCurrentWaterIntake >= user.getTotalWaterIntake();
                    DailyWaterIntake dailyIntake = new DailyWaterIntake(today, goalAchieved);
                    if (user.getDailyWaterIntakes() == null) {
                        user.setDailyWaterIntakes(new HashMap<>());
                    }
                    user.getDailyWaterIntakes().put(today, dailyIntake);

                    //felhasználó adatbázisban történő frissítése az új adatokkal
                    mDatabase.child("users").child(uid).setValue(user).addOnCompleteListener(updateTask -> {
                        //ellenőrizzük, hogy a frissítés sikeres volt-e
                        if (updateTask.isSuccessful()) {
                            //frissítjük a UI-t az új vízfogyasztási adatokkal
                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            String formattedValue = decimalFormat.format(newCurrentWaterIntake);
                            binding.currentEditText.setText(formattedValue);

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


    private void updateAnimation(float newCurrentWaterIntake, double totalWaterIntake, boolean goalAchieved) {
        float progress = newCurrentWaterIntake / (float) totalWaterIntake;
        binding.animationView.setProgress(progress);
        if (goalAchieved) {
            playSuccessAnimation();
        }
    }


    private void playSuccessAnimation() {
        LottieAnimationView successAnimation = binding.successAnimation;
        successAnimation.setVisibility(View.VISIBLE); // láthatóvá tétel
        successAnimation.setAnimation("success2.json");
        successAnimation.playAnimation();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}