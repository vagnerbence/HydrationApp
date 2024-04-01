package hu.app.hydrationapp.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import hu.app.hydrationapp.ui.login.LoginViewModel;

public class LoginFragment extends Fragment {

    FirebaseAuth mAuth;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_login_home_to_navigation_home);
        }
    }

    private FragmentLoginBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LoginViewModel loginViewModel =
                new ViewModelProvider(this).get(LoginViewModel.class);

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        TextInputEditText emailEditText = binding.emailLoginEditText;
        TextInputEditText passwordEditText = binding.passwordLoginEditText;
        Button loginButton = binding.loginButton;



        final TextView textView = binding.textLogin;
        loginViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);





       /* Button loginButton = binding.loginButton;
        loginButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_login_home_to_navigation_home);

        });*/

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(emailEditText.getText());
                password = String.valueOf(passwordEditText.getText());
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(getContext(), "Enter E-mail", Toast.LENGTH_SHORT).show();
                    return;
                } if (TextUtils.isEmpty(password)){
                    Toast.makeText(getContext(), "Enter  Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(getContext(), "Login is successful.",
                                            Toast.LENGTH_SHORT).show();
                                        NavHostFragment.findNavController(LoginFragment.this)
                                                .navigate(R.id.action_login_home_to_navigation_home);


                                } else {

                                    Toast.makeText(getContext(), "Login failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });


        Button registrationButton = binding.registrationButton;
        registrationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_login_home_to_navigation_registration);

        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}