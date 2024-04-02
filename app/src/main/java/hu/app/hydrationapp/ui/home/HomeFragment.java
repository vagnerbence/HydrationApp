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

import hu.app.hydrationapp.databinding.FragmentHomeBinding;
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getValue(User.class) != null) {
                    User user = task.getResult().getValue(User.class);
                    // Ha a felhasználó új, és a currentWaterIntake 0, beállítjuk a kezdő progresszt
                    float startingProgress = user.getCurrentWaterIntake() == 0 ? 0f : binding.animationView.getProgress();

                    float newCurrentWaterIntake = Math.min(user.getCurrentWaterIntake() + amount, (float) user.getTotalWaterIntake());
                    user.setCurrentWaterIntake(newCurrentWaterIntake);

                    mDatabase.child("users").child(currentUser.getUid()).setValue(user).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            String formattedValue = decimalFormat.format(newCurrentWaterIntake);
                            binding.currentEditText.setText(formattedValue);

                            float progress = newCurrentWaterIntake / (float) user.getTotalWaterIntake();

                            // Ellenőrizzük, hogy az animáció ne kezdődjön nagyobb progressznél
                            if (startingProgress < progress) {
                                binding.animationView.setMinAndMaxProgress(startingProgress, progress);
                            } else {
                                binding.animationView.setMinAndMaxProgress(0f, progress);
                            }
                            binding.animationView.playAnimation();

                            if (newCurrentWaterIntake >= user.getTotalWaterIntake()) {
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
        successAnimation.setVisibility(View.VISIBLE); // Ha el volt rejtve, tedd láthatóvá
        successAnimation.setAnimation("success2.json"); // Ellenőrizd, hogy ez megfelel-e az animációd fájlnevénk
        successAnimation.playAnimation(); // Lejátszás indítása
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
