package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Project;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Schedules;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

public class DetailScheduleActivity extends AppCompatActivity {
    public static final String PROJECT_ID = "id";
    public static final String TITLE = "title";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String CONTENT = "content";

    private int projectId;
    private String title;
    private String date;
    private String time;
    private String content;

    private TextView titleTextView;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView contentTextView;
    private TextView projectNameTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_details);

        titleTextView = findViewById(R.id.titleTextView);
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);
        contentTextView = findViewById(R.id.contentTextView);
        projectNameTextView = findViewById(R.id.projectNameTextView);

        Intent intent = getIntent();
        title = intent.getStringExtra(TITLE);
        date = intent.getStringExtra(DATE);
        time = intent.getStringExtra(TIME);
        content = intent.getStringExtra(CONTENT);
        projectId = intent.getIntExtra(PROJECT_ID, DataManager.NOT_SETUP_I);

        titleTextView.setText(title);
        dateTextView.setText(date);
        timeTextView.setText(time);
        contentTextView.setText(content);

        LocalDBMain.GetTable(DB_Project.class).getDefaultDataCursorById(projectId).execute((cursor)->{
            if(cursor.moveToFirst()){
                projectNameTextView.setText(cursor.getString(1));
            }
        });
    }
}
