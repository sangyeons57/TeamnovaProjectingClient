package com.example.teamnovapersonalprojectprojecting.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.Calendar24HourActivity;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Schedules;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.Calendar;

public class CalendarFragment extends Fragment {
    private GridView calendarGrid;
    private ArrayList<String> days;
    private CalendarAdapter calendarAdapter;

    private TextView monthYearText;
    private int currentDay;
    private int currentMonth;
    private int currentYear;

    public static int showYear;
    public static int showMonth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarGrid = view.findViewById(R.id.calendar_grid);
        monthYearText = view.findViewById(R.id.month_year_text);
        days = new ArrayList<>();

        calendarGrid.setVerticalScrollBarEnabled(false);
        calendarGrid.setHorizontalFadingEdgeEnabled(false);

        // 현재 날짜 가져오기
        Calendar calendar = Calendar.getInstance();
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentMonth = calendar.get(Calendar.MONTH); // 0-11, 0=January
        currentYear = calendar.get(Calendar.YEAR);

        showYear = currentYear;
        showMonth = currentMonth;

        calendarAdapter = new CalendarAdapter(DataManager.Instance().currentContext, days, currentDay, currentMonth, currentYear);
        calendarGrid.setAdapter(calendarAdapter);

        calendarGrid.setOnTouchListener(new View.OnTouchListener() {
            private float initialY = 0;  // 터치 시작 Y 좌표
            private boolean isScrollingUp = false;
            private boolean isScrollingDown = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 터치가 시작될 때 Y 좌표를 저장
                        initialY = event.getY();
                        isScrollingUp = false;
                        isScrollingDown = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float currentY = event.getY();
                        float deltaY = currentY - initialY;  // Y 좌표의 변화량을 계산

                        if (deltaY > 0 && !isScrollingDown) {
                            // 사용자가 아래로 스와이프한 경우
                            isScrollingUp = false;
                            isScrollingDown = true;
                            Log.d("GridView", "Swiping Down");
                        } else if (deltaY < 0 && !isScrollingUp) {
                            // 사용자가 위로 스와이프한 경우
                            isScrollingUp = true;
                            isScrollingDown = false;
                            Log.d("GridView", "Swiping Up");
                        }

                        // 현재 Y 좌표를 업데이트하여 지속적인 터치 이동을 추적
                        initialY = currentY;
                        break;

                    case MotionEvent.ACTION_UP:
                        // 터치가 끝날 때 상태 초기화
                        if (isScrollingUp && !isScrollingDown) {
                            addOneMonth();
                        } else if (!isScrollingUp && isScrollingDown) {
                            minusOneMonth();
                        }
                        break;
                }
                return false;  // false를 반환하여 다른 터치 이벤트도 계속 처리되도록 함
            }
        });

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_SCHEDULE_DATA, (jsonUtil) -> {
            JSONArray jsonArray = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    LocalDBMain.GetTable(DB_Schedules.class).addSchedule(jsonObject.getInt("id"), jsonObject.getInt("project_id"), jsonObject.getString("date"), new JSONObject(jsonObject.getString("data")));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            DataManager.Instance().mainHandler.post(() -> calendarAdapter.notifyDataSetChanged());
            return false;
        });



        //월과 년도 표시
        calendarGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String day = days.get(position);
                if (!day.isEmpty()) {
                    Toast.makeText(view.getContext(), "날짜 클릭됨: " + day, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), Calendar24HourActivity.class);
                    intent.putExtra(Calendar24HourActivity.YEAR, showYear);
                    intent.putExtra(Calendar24HourActivity.MONTH, showMonth + 1);
                    intent.putExtra(Calendar24HourActivity.DAY, Integer.parseInt(day));
                    startActivity(intent);
                }
            }
        });

        showCalendar(calendar);
        return view;
    }

    public void addOneMonth() {
        ++showMonth;
        if(showMonth > 11){
            showMonth = 0;
            ++showYear;
        }
        showCalendar(Calendar.getInstance());
    }
    public void minusOneMonth(){
        --showMonth;
        if(showMonth < 0){
            showMonth = 11;
            --showYear;
        }
        showCalendar(Calendar.getInstance());
    }

    public void showCalendar(Calendar calendar){
        monthYearText.setText(showYear + "년 "  + (showMonth + 1) + "월");


        calendar.set(Calendar.YEAR, showYear);
        calendar.set(Calendar.MONTH, showMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        days.clear();
        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(""); // 빈 날짜
        }

        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }

        try {
            SocketConnection.sendMessage(new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_SCHEDULE_DATA.toString())
                    .add(JsonUtil.Key.DATETIME, new JSONObject()
                            .put("year", showYear)
                            .put("month", showMonth + 1)));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}