package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Schedules;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.calendar.EditScheduleDialogFragment;
import com.example.teamnovapersonalprojectprojecting.ui.calendar.TimeRangeViewGroup;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.Retry;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Calendar24HourActivity extends AppCompatActivity {
    public static final String YEAR = "YEAR";
    public static final String MONTH = "MONTH";
    public static final String DAY = "DAY";

    private TimeRangeViewGroup timeRangeViewGroup;
    private TextView dateTextView;
    private Button addScheduleButton;

    int yearValue;
    int monthValue;
    int dayValue;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_24hour);
        DataManager.Instance().currentContext = this;

        timeRangeViewGroup = findViewById(R.id.timeRangeViewGroup);
        dateTextView = findViewById(R.id.dateTextView);
        addScheduleButton = findViewById(R.id.addScheduleButton);

        Intent intent = getIntent();
        yearValue = intent.getIntExtra(YEAR, DataManager.NOT_SETUP_I);
        monthValue = intent.getIntExtra(MONTH,DataManager.NOT_SETUP_I);
        dayValue = intent.getIntExtra(DAY, DataManager.NOT_SETUP_I);

        dateTextView.setText(String.format("%d년 %d월 %d일", yearValue, monthValue, dayValue));

        addScheduleButton.setOnClickListener((view)->{
            Intent newIntent = new Intent(this, AddScheduleActivity.class);
            newIntent.putExtra(YEAR, yearValue);
            newIntent.putExtra(MONTH, monthValue);
            newIntent.putExtra(DAY, dayValue);
            startActivity(newIntent);
        });
        showSchedule();

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_SCHEDULE_DATA, (jsonUtil) ->{
            showSchedule();
            return false;
        });
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
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
    }

    public void showSchedule(){
        new Retry(()->{
            DataManager.Instance().mainHandler.post(()->{
                timeRangeViewGroup.clear();
            });
            try {
                LocalDBMain.GetTable(DB_Schedules.class).getScheduleByDay(LocalDate.of(yearValue, monthValue, dayValue)).execute((cursor)->{
                    Log.d(Calendar24HourActivity.class.getSimpleName(), "count: "+cursor.getCount());
                    while (cursor.moveToNext()){
                        int id = cursor.getInt(0);
                        int projectId = cursor.getInt(1);
                        LocalDate date = LocalDate.parse(cursor.getString(2));
                        LocalTime startTime;
                        LocalTime endTime;

                        String title;
                        String content;
                        try {
                            JSONObject data = new JSONObject(cursor.getString(3));

                            startTime = LocalTime.parse(data.getString("startTime"));
                            endTime = LocalTime.parse(data.getString("endTime"));

                            title = data.getString("title");
                            content = data.getString("content");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        DataManager.Instance().mainHandler.post(()-> {
                            Log.d(Calendar24HourActivity.class.getSimpleName(), "id: " + id + " title: " + title);
                            TimeRangeViewGroup.RangeItem  rangeItem = new TimeRangeViewGroup.RangeItem(DataManager.convertToDecimal(startTime), DataManager.convertToDecimal(endTime), title);
                            rangeItem.setListener( new TimeRangeViewGroup.OnRangeItemClickListener(){

                                @Override
                                public void onRangeItemClick() {
                                    Intent newIntent = new Intent(Calendar24HourActivity.this, DetailScheduleActivity.class);
                                    newIntent.putExtra(DetailScheduleActivity.PROJECT_ID, projectId);
                                    newIntent.putExtra(DetailScheduleActivity.TITLE, title);
                                    newIntent.putExtra(DetailScheduleActivity.CONTENT, content);
                                    newIntent.putExtra(DetailScheduleActivity.DATE, date.toString());
                                    newIntent.putExtra(DetailScheduleActivity.TIME, startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " ~ " + endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                                    startActivity(newIntent);
                                }

                                @Override
                                public boolean onRangeItemLongClick() {
                                    EditScheduleDialogFragment.Instance(id).show(getSupportFragmentManager(), "EditScheduleDialogFragment");
                                    return TimeRangeViewGroup.OnRangeItemClickListener.super.onRangeItemLongClick();
                                }
                            });
                            timeRangeViewGroup.addRangeItemView(rangeItem);
                        });
                    }
                });
            } catch (IllegalStateException e){
                e.printStackTrace();
                return false;
            }
            return true;
        }).setMaxRetries(5).setRetryInterval(100).execute();
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }
}
