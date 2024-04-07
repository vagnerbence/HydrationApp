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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Példa adatok a demonstrációhoz
        Map<String, DailyWaterIntake> dailyWaterIntakes = new HashMap<>();
        // Itt kellene betölteni a valós adatokat a User objektumból vagy az adatbázisból

        // Események generálása és hozzáadása a naptárhoz
        List<CalendarDay> calendarDays = generateCalendarDays(dailyWaterIntakes);
        binding.calendarView.setCalendarDays(calendarDays);

        return root;
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

    private List<CalendarDay> generateCalendarDays(Map<String, DailyWaterIntake> dailyWaterIntakes) {
        List<CalendarDay> calendarDays = new ArrayList<>();

        for (Map.Entry<String, DailyWaterIntake> entry : dailyWaterIntakes.entrySet()) {
            Calendar calendar = stringToCalendar(entry.getKey());
            DailyWaterIntake dailyWaterIntake = entry.getValue();

            if (dailyWaterIntake.isGoalAchieved()) {
                CalendarDay calendarDay = new CalendarDay(calendar);
                calendarDay.setImageResource(R.drawable.baseline_check_24 ); // Ellenőrizd, hogy ez a drawable létezik-e
                calendarDay.setLabelColor(Color.parseColor("#228B22")); // Zöld színű címke
                calendarDays.add(calendarDay);
            }
        }
        return calendarDays;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
