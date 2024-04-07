package hu.app.hydrationapp.ui.registration;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hu.app.hydrationapp.R;
import hu.app.hydrationapp.databinding.FragmentLoginBinding;
import hu.app.hydrationapp.databinding.FragmentRegistrationBinding;
import hu.app.hydrationapp.ui.login.LoginFragment;
import hu.app.hydrationapp.ui.registration.RegistrationViewModel;

public class RegistrationFragment extends Fragment {

FirebaseAuth mAuth;
ProgressBar progressBar;
    private FragmentRegistrationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RegistrationViewModel registrationViewModel =
                new ViewModelProvider(this).get(RegistrationViewModel.class);
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        mAuth = FirebaseAuth.getInstance();
        TextInputEditText emailEditText = binding.emailEditText;
        TextInputEditText passwordEditText = binding.passwordEditText;
        Button saveRegistrationButton = binding.saveRegistrationButton;
        ProgressBar progressBar = binding.progressBar;
        TextView textRegistration = binding.textRegistration;
        textRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(RegistrationFragment.this)
                        .navigate(R.id.action_navigation_registration_to_login_home);
            }
        });



        saveRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(emailEditText.getText());
                password = String.valueOf(passwordEditText.getText());
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(getContext(), "Add E-mail", Toast.LENGTH_SHORT).show();
                    return;
                } if (TextUtils.isEmpty(password)){
                    Toast.makeText(getContext(), "Add Password", Toast.LENGTH_SHORT).show();
                    return;
                }


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    Toast.makeText(getContext(), "User is created.",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(getContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

        final TextView textView = binding.textRegistration;
        registrationViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);






      /*  Button saveRegistrationButton = binding.saveRegistrationButton;
        saveRegistrationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(RegistrationFragment.this)
                    .navigate(R.id.action_navigation_registration_to_navigation_home);
        });*/
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}