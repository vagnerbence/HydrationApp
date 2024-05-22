package hu.app.hydrationapp.ui.logout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hu.app.hydrationapp.R;
import hu.app.hydrationapp.databinding.FragmentLogoutBinding;
import hu.app.hydrationapp.ui.logout.LogoutViewModel;
import hu.app.hydrationapp.ui.registration.RegistrationFragment;


// ... [Az előző importok elhelyezése] ...

public class LogoutFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FragmentLogoutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogoutViewModel logoutViewModel =
                new ViewModelProvider(this).get(LogoutViewModel.class);

        binding = FragmentLogoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //felhasználó állapota
        if (user == null) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigation_logout_to_navigation_home);
        } else {

            final TextView textView = binding.textLogout;
            logoutViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

            //kijelentkezés gomb kezelése
            Button logoutButton = binding.logoutButton;
            logoutButton.setOnClickListener(v -> {
                mAuth.signOut(); // kijelentkezés a Firebase-ból
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigation_logout_to_navigation_home);
            });
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

