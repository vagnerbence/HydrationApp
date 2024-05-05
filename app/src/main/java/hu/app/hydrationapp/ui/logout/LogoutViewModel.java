package hu.app.hydrationapp.ui.logout;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

import hu.app.hydrationapp.model.User;

public class LogoutViewModel extends ViewModel {

    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public LogoutViewModel() {
        updateMessage();
    }

    private void updateMessage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    User userSettings = task.getResult().getValue(User.class);
                    if (userSettings != null && userSettings.getName() != null) {
                        mText.setValue("Goodbye " + userSettings.getName() + "!");
                    } else {
                        mText.setValue("Goodbye!");
                    }
                } else {
                    mText.setValue("Goodbye!");
                }
            });
        } else {
            mText.setValue("Goodbye!");
        }
    }

    public LiveData<String> getText() {
        return mText;
    }
}
