package hu.app.hydrationapp.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import hu.app.hydrationapp.R;
import hu.app.hydrationapp.databinding.FragmentLoginBinding;
import hu.app.hydrationapp.MainActivity;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mAuth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(v -> attemptLogin());

//regisztrációra vezető:
        Button registrationButton = binding.registrationButton;
        registrationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_login_home_to_navigation_registration);

        });

        return root;
    }

    private void attemptLogin() {
        String email = binding.emailLoginEditText.getText().toString();
        String password = binding.passwordLoginEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Email and password must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showBottomNav(true);
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_login_home_to_navigation_home);
            } else {
                Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBottomNav(boolean show) {
        ((MainActivity)getActivity()).setBottomNavVisibility(show);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            showBottomNav(true);
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_login_home_to_navigation_home);
        } else {
            showBottomNav(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
