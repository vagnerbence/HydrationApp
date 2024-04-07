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

public class LogoutFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseUser user;

    private FragmentLogoutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogoutViewModel logoutViewModel =
                new ViewModelProvider(this).get(LogoutViewModel.class);

        binding = FragmentLogoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        Button logoutButton = binding.logoutButton;
        user = mAuth.getCurrentUser();
if (user==null){
            NavHostFragment.findNavController(LogoutFragment.this)
                    .navigate(R.id.action_navigation_logout_to_navigation_home);
        }
else {
    logoutButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(LogoutFragment.this)
                    .navigate(R.id.action_navigation_logout_to_navigation_home);
        }
    });
}

        final TextView textView = binding.textLogout;
        logoutViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

      /*  Button logoutButton = binding.logoutButton;
        logoutButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(LogoutFragment.this)
                    .navigate(R.id.action_navigation_logout_to_navigation_home);
        });*/

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
