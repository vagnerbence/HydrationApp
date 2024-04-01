package hu.app.hydrationapp.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

import hu.app.hydrationapp.R;
import hu.app.hydrationapp.databinding.FragmentSettingsBinding;
import hu.app.hydrationapp.model.HydrationCalculator;
import hu.app.hydrationapp.model.User;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupActivityLevelSpinner();

        binding.buttonSave.setOnClickListener(view -> saveUserSettings());

        loadUserSettings(); // Betöltjük a felhasználó beállításait az adatbázisból

        return root;
    }

    private void setupActivityLevelSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.activity_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.activityLevelSpinner.setAdapter(adapter);
    }

    private void loadUserSettings() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Ellenőrizzük, hogy léteznek-e mentett beállítások a jelenlegi felhasználóhoz
                    if (!dataSnapshot.exists()) {
                        // Ha nem léteznek adatok, akkor tisztítjuk a UI mezőket
                        clearUserDataInView();
                        return; // Ezzel befejezzük a metódus futtatását
                    }

                    // Ha vannak mentett adatok, betöltjük őket
                    User userSettings = dataSnapshot.getValue(User.class);
                    if (userSettings != null) {
                        // Itt töltsd be a felhasználó adatait a UI elemekbe
                        binding.nameEditText.setText(userSettings.getName());
                        binding.heightEditText.setText(String.valueOf(userSettings.getHeight()));
                        binding.weightEditText.setText(String.valueOf(userSettings.getWeight()));
                        binding.ageEditText.setText(String.valueOf(userSettings.getAge()));

                        // A nem kiválasztása a RadioButton alapján
                        if ("Male".equals(userSettings.getGender())) {
                            binding.genderRadioGroup.check(R.id.maleRadioButton);
                        } else if ("Female".equals(userSettings.getGender())) {
                            binding.genderRadioGroup.check(R.id.femaleRadioButton);
                        }

                        // Az aktivitási szint kiválasztása a Spinner-ben
                        String activityLevel = userSettings.getActivityLevel();
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.activityLevelSpinner.getAdapter();
                        int position = adapter.getPosition(activityLevel);
                        binding.activityLevelSpinner.setSelection(position);

                        // Az összesített napi vízbevitel megjelenítése
                        DecimalFormat df = new DecimalFormat("#.##");
                        String formattedTotalWaterIntake = df.format(userSettings.getTotalWaterIntake());
                        binding.quantityEditText.setText(formattedTotalWaterIntake);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load settings.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveUserSettings() {
        String name = binding.nameEditText.getText().toString().trim();
        int height = Integer.parseInt(binding.heightEditText.getText().toString().trim());
        int weight = Integer.parseInt(binding.weightEditText.getText().toString().trim());
        int age = Integer.parseInt(binding.ageEditText.getText().toString().trim());
        String gender = binding.genderRadioGroup.getCheckedRadioButtonId() == R.id.maleRadioButton ? "Male" : "Female";
        String activityLevel = binding.activityLevelSpinner.getSelectedItem().toString();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            double activityWater = HydrationCalculator.calculateActivityWater(activityLevel);
            double baseWater = HydrationCalculator.calculateBaseWater(weight, age,gender);
            double totalWaterIntake = activityWater + baseWater;

            // Az új adatok frissítése a User objektumban
            User userData = new User(name, height, weight, age, gender, activityLevel, totalWaterIntake);

            // A frissített adatok mentése az adatbázisba
            mDatabase.child("users").child(user.getUid()).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Sikeres mentés esetén frissítjük a felhasználó felületét is
                        Toast.makeText(getContext(), "Settings saved successfully.", Toast.LENGTH_SHORT).show();

                        // Betöltjük a frissített adatokat újra azonnal, hogy megjelenjenek a felületen
                        loadUserSettings();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save settings.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "You need to be logged in to update settings.", Toast.LENGTH_SHORT).show();
        }
    }
    private void clearUserDataInView() {
        binding.nameEditText.setText("");
        binding.heightEditText.setText("");
        binding.weightEditText.setText("");
        binding.ageEditText.setText("");
        binding.genderRadioGroup.clearCheck();
        binding.activityLevelSpinner.setSelection(0);
        binding.quantityEditText.setText("");
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
