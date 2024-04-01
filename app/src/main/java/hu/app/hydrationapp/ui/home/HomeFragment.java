package hu.app.hydrationapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import hu.app.hydrationapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private float currentProgress = 0f; // Jelenlegi progresszív állapot tárolása

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final LottieAnimationView animationView = binding.animationView;

        // Gombok inicializálása
        ImageButton btnFivePercent = binding.btnFivePercent;
        ImageButton btnTenPercent = binding.btnTenPercent;
        ImageButton btnTwentyFivePercent = binding.btnTwentyFivePercent;
        ImageButton btnFiftyPercent = binding.btnFiftyPercent;

        // Gombokra kattintás eseményeinek kezelése
        setUpButton(btnFivePercent, 0.05f, animationView);
        setUpButton(btnTenPercent, 0.1f, animationView);
        setUpButton(btnTwentyFivePercent, 0.25f, animationView);
        setUpButton(btnFiftyPercent, 0.5f, animationView);

        return root;
    }

    private void setUpButton(ImageButton button, float increment, LottieAnimationView animationView) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProgress(increment, animationView);
            }
        });
    }

    private void updateProgress(float increment, LottieAnimationView animationView) {
        float startProgress = currentProgress;
        currentProgress += increment;
        if (currentProgress > 1.0f) {
            currentProgress = 1.0f;
        }

        animationView.setMinAndMaxProgress(startProgress, currentProgress);
        animationView.playAnimation();

        // Ha elérjük az 1.0f-t, indítsuk el a siker animációt
        if (currentProgress == 1.0f) {
            playSuccessAnimation();
        }
    }

    private void playSuccessAnimation() {
        LottieAnimationView successAnimation = binding.successAnimation;
        successAnimation.setVisibility(View.VISIBLE); // Ha el volt rejtve, tedd láthatóvá
        successAnimation.setAnimation("success2.json"); // Cseréld le a te animációs fájlodra
        successAnimation.playAnimation(); // Lejátszás indítása
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
