package com.example.teamnovapersonalprojectprojecting.activity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Project;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Schedules;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddScheduleActivity extends AppCompatActivity {
    private Spinner projectSpinner;
    private TextView timeTextView;
    private EditText scheduleNameTextView;
    private EditText scheduleContentTextView;
    private Button finishButton;

    private int startHour, startMinute, endHour, endMinute;
    private boolean isStartTimeSet = false;
    private boolean isEndTimeSet = false;
    private boolean isTimeSet = false;

    private int selectedProject;
    private LocalDate selectedDate;

    private Map<String, Integer> projectIdMap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_add_schedule);
        DataManager.Instance().currentContext = this;

        projectSpinner = findViewById(R.id.project_name_spinner);
        timeTextView = findViewById(R.id.time_textview);
        scheduleNameTextView = findViewById(R.id.schedule_name_textview);
        scheduleContentTextView = findViewById(R.id.schedule_content_textview);
        finishButton = findViewById(R.id.finish_button);

        Intent intent = getIntent();
        int yearValue = intent.getIntExtra(Calendar24HourActivity.YEAR, DataManager.NOT_SETUP_I);
        int monthValue = intent.getIntExtra(Calendar24HourActivity.MONTH, DataManager.NOT_SETUP_I);
        int dayValue = intent.getIntExtra(Calendar24HourActivity.DAY, DataManager.NOT_SETUP_I);
        selectedDate = LocalDate.of(yearValue, monthValue, dayValue);

        projectIdMap = new HashMap<>();
        selectedProject = DataManager.NOT_SETUP_I;

        List<String> spinnerData = new ArrayList<>();
        LocalDBMain.GetTable(DB_Project.class).getDefaultDataCursor().execute((cursor)->{
            while (cursor.moveToNext()){
                projectIdMap.put(cursor.getString(1), cursor.getInt(0));
                spinnerData.add(cursor.getString(1));
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerData);

        projectSpinner.setAdapter(adapter);
        projectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedProject = projectIdMap.get(selectedItem);
                // Toast.makeText(AddScheduleActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        timeTextView.setOnClickListener(v -> {
            // 첫 번째 TimePicker (시작 시간)
            TimePickerDialog startTimePicker = new TimePickerDialog(AddScheduleActivity.this, (view, hourOfDay, minute) -> {
                startHour = hourOfDay;
                startMinute = minute;
                isStartTimeSet = true;

                // 두 번째 TimePicker (끝나는 시간)
                TimePickerDialog endTimePicker = new TimePickerDialog(AddScheduleActivity.this, (view2, endHourOfDay, endMinuteOfDay) -> {
                    endHour = endHourOfDay;
                    endMinute = endMinuteOfDay;
                    isEndTimeSet = true;

                    // 시간을 비교하여 모순 확인 (시작 시간이 끝나는 시간보다 뒤인 경우)
                    if (isTimeInvalid()) {
                        Toast.makeText(AddScheduleActivity.this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                    } else {
                        // 유효한 시간 범위 표시
                        timeTextView.setText(formatTime(startHour, startMinute) + " ~ " + formatTime(endHour, endMinute));
                        isTimeSet = true;
                    }

                }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);

                // 두 번째 TimePicker 표시
                endTimePicker.show();

            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);

            // 첫 번째 TimePicker 표시
            startTimePicker.show();
        });

        finishButton.setOnClickListener((view)->{
            if(scheduleNameTextView.getText().toString().trim().isEmpty()){
                Toast.makeText(AddScheduleActivity.this, "Title is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isTimeSet){
                Toast.makeText(AddScheduleActivity.this, "time is not set up", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject data = new JSONObject()
                        .put("title" , scheduleNameTextView.getText().toString())
                        .put("content", scheduleContentTextView.getText().toString())
                        .put("startTime", LocalTime.of(startHour, startMinute).format(DateTimeFormatter.ofPattern("HH:mm")))
                        .put("endTime", LocalTime.of(endHour, endMinute).format(DateTimeFormatter.ofPattern("HH:mm")));

                SocketConnection.sendMessage(new JsonUtil()
                        .add(JsonUtil.Key.TYPE, SocketEventListener.eType.ADD_SCHEDULE)
                        .add(JsonUtil.Key.PROJECT_ID, selectedProject)
                        .add(JsonUtil.Key.DATETIME, selectedDate.toString())
                        .add(JsonUtil.Key.DATA, data));

                SocketEventListener.addAddEventQueue(SocketEventListener.eType.ADD_SCHEDULE, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.ADD_SCHEDULE){
                    @Override
                    public boolean runOnce(JsonUtil jsonUtil) {
                        if(jsonUtil.getBoolean(JsonUtil.Key.IS_VALID, false)){
                            int id = jsonUtil.getInt(JsonUtil.Key.ID, DataManager.NOT_SETUP_I);
                            LocalDBMain.GetTable(DB_Schedules.class).addSchedule(id, selectedProject, selectedDate.toString(), data);

                            finish();
                            try {
                                SocketConnection.sendMessage(new JsonUtil()
                                        .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_SCHEDULE_DATA.toString())
                                        .add(JsonUtil.Key.DATETIME, new JSONObject()
                                                .put("year", yearValue)
                                                .put("month", monthValue)
                                                .put("day", dayValue)));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Toast.makeText(AddScheduleActivity.this, "Fail to add schedule", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    // 시간 비교 (시작 시간이 끝나는 시간보다 늦은지 확인)
    private boolean isTimeInvalid() {
        Log.d(AddScheduleActivity.class.getSimpleName(), startHour +" " + startMinute + " " + endHour + " " + endMinute);
        if (startHour > endHour) {
            return true;
        } else if (startHour == endHour && startMinute >= endMinute) {
            return true;
        }
        return false;
    }

    // 시간을 보기 좋게 포맷하는 메서드
    private String formatTime(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }
}
