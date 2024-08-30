package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Project;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class ProjectSettingActivity extends AppCompatActivity {

    private EditText projectNameEditText;
    private Button deleteProjectButton;
    private Button saveProjectSettingButton;

    private int projectId;
    private String projectName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_setting);

        projectNameEditText = findViewById(R.id.projectNameEditText);
        deleteProjectButton = findViewById(R.id.deleteProjectButton);
        saveProjectSettingButton = findViewById(R.id.saveProjectSettingButton);


        this.projectId = getIntent().getIntExtra(ProjectSettingDialogFragment.PROJECT_ID,0);
        this.projectName = getIntent().getStringExtra(ProjectSettingDialogFragment.PROJECT_NAME);

        projectNameEditText.setText(projectName);

        deleteProjectButton.setOnClickListener(this::onClickDeleteProjectButton);
        saveProjectSettingButton.setOnClickListener(this::onClickSaveProjectButton);
    }

    public void onClickDeleteProjectButton(View view){

    }

    public void onClickSaveProjectButton(View view){
        finish();
        String newName = projectNameEditText.getText().toString();
        DataManager.Instance().projectName = newName;
        LocalDBMain.GetTable(DB_Project.class).updateNameById(projectId, newName);

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EDIT_PROJECT_NAME.toString())
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.NAME, newName)
        );

        SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString())
        );
    }
}
