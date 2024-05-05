package hu.app.hydrationapp.ui.notifications;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.CalendarDay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hu.app.hydrationapp.R;
import hu.app.hydrationapp.databinding.FragmentNotificationsBinding;
import hu.app.hydrationapp.model.DailyWaterIntake;
import hu.app.hydrationapp.model.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    private void loadUserAndDailyIntakes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null && user.getDailyWaterIntakes() != null) {

                        // Itt frissítjük a naptárt a megfelelő adatokkal
                        List<CalendarDay> calendarDays = generateCalendarDays(user.getDailyWaterIntakes());
                        binding.calendarView.setCalendarDays(calendarDays);

                    } else {
                        // Kezeljük az esetet, ha nincsenek felhasználói adatok
                        // Például megjeleníthetünk egy üzenetet, vagy navigálhatunk egy másik képernyőre
                    }
                }
            });
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Itt hívjuk meg az adatok betöltésére szolgáló függvényt
        loadUserAndDailyIntakes();

        return root;
    }


    private List<CalendarDay> generateCalendarDays(Map<String, DailyWaterIntake> dailyWaterIntakes) {
        List<CalendarDay> calendarDays = new ArrayList<>();

        for (Map.Entry<String, DailyWaterIntake> entry : dailyWaterIntakes.entrySet()) {
            String dateStr = entry.getKey();
            DailyWaterIntake dailyWaterIntake = entry.getValue();

            Calendar calendar = stringToCalendar(dateStr);
            boolean goalAchieved = dailyWaterIntake.isGoalAchieved();

            CalendarDay calendarDay = new CalendarDay(calendar);
            if (goalAchieved) {
                // A cél elérése esetén zöld pipa vagy más jelzés beállítása
                calendarDay.setImageDrawable(getResources().getDrawable(R.drawable.baseline_check_24));

            } else {
                // Ha a cél nem lett elérve, piros X vagy más jelzés
                calendarDay.setImageDrawable(getResources().getDrawable(R.drawable.baseline_clear_24));

            }

            calendarDays.add(calendarDay);
        }

        return calendarDays;
    }

    private Calendar stringToCalendar(String dateStr) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            cal.setTime(sdf.parse(dateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
