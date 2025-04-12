package com.example.teamnovapersonalprojectprojecting.ui.calendar;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Schedules;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.util.Retry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> days;

    private int currentDay;
    private int currentMonth;
    private int currentYear;
    private Calendar calendar;

    public CalendarAdapter(Context context, ArrayList<String> days, int currentDay, int currentMonth, int currentYear) {
        this.context = context;
        this.days = days;
        this.currentDay = currentDay;
        this.currentMonth = currentMonth;
        this.currentYear = currentYear;
        this.calendar = Calendar.getInstance();

        LocalDBMain.printAllRows(DB_Schedules.class);
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }
        ViewGroup.LayoutParams params = convertView.getLayoutParams();
        params.height = (int) (parent.getHeight() / 5);
        convertView.setLayoutParams(params);

        TextView dayText = convertView.findViewById(R.id.day_text);
        ImageView scheduleIndicator = convertView.findViewById(R.id.schedule_indicator_imageview);
        scheduleIndicator.setBackgroundColor(Color.TRANSPARENT);
        dayText.setText(days.get(position));
        dayText.setBackgroundColor(Color.TRANSPARENT); // 다른 날짜는 투명 배경

        // 현재 날짜와 일치하는 경우 색상 변경
        if (!days.get(position).isEmpty()) {
            int day = Integer.parseInt(days.get(position));
            calendar.set(Calendar.YEAR, CalendarFragment.showYear);
            calendar.set(Calendar.MONTH, CalendarFragment.showMonth);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            // Log.d(CalendarAdapter.class.getSimpleName(), position + " " + day + " " + calendar.get(Calendar.DAY_OF_MONTH) + " " + (calendar.get(Calendar.MONTH) + 1) + " " + calendar.get(Calendar.YEAR));

            new Retry(()->{
                try {
                    if (LocalDBMain.GetTable(DB_Schedules.class).isScheduleExist(LocalDate.of(CalendarFragment.showYear, CalendarFragment.showMonth + 1, calendar.get(Calendar.DAY_OF_MONTH)))){
                        scheduleIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.default_app_color_d1));
                    }
                } catch (IllegalStateException e){
                    e.printStackTrace();
                    return false;
                }
                return true;
            }).setMaxRetries(5).setRetryInterval(100).execute();

            if (calendar.get(Calendar.DAY_OF_MONTH) == currentDay &&
                    calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.YEAR) == currentYear) {
                convertView.setBackgroundColor(0xFF97C6CA); // 현재 날짜 배경색
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT); // 현재 날짜 배경색
            }
        }

        return convertView;
    }
}
