package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;

public class EditRoleActivity extends AppCompatActivity {
    public static final String IS_ADD_MODE = "isAddMode";
    public static final String PROJECT_ID = "projectId";

    private int projectId;

    private EditText roleNameEditText;

    private Button saveButton;
    private Switch authorityMemberInviteSwitch;
    private Switch authoritySettingProjectSwitch;
    private Switch authorityEditProjectStructureSwitch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager.Instance().currentContext = this;
        setContentView(R.layout.activity_edit_role);

        roleNameEditText = findViewById(R.id.roleNameEditText);
        authorityMemberInviteSwitch = findViewById(R.id.authority_memeber_invite_switch);
        authoritySettingProjectSwitch = findViewById(R.id.authority_setting_project_switch);
        authorityEditProjectStructureSwitch = findViewById(R.id.authority_edit_project_structure_switch);
        saveButton = findViewById(R.id.saveButton);

        authorityMemberInviteSwitch.isChecked();

        Intent intent = getIntent();
        intent.getBooleanExtra(IS_ADD_MODE, false);
        projectId = intent.getIntExtra(PROJECT_ID, DataManager.NOT_SETUP_I);

        saveButton.setOnClickListener(this::onClickSaveButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    public JSONArray getAttributes(){
        JSONArray attributes = new JSONArray();
        if (authorityMemberInviteSwitch.isChecked()) {
            attributes.put("AuthorityMemberInvite");
        }
        if (authorityEditProjectStructureSwitch.isChecked()) {
            attributes.put("AuthorityEditProjectStructure");
        }
        if (authoritySettingProjectSwitch.isChecked()) {
            attributes.put("AuthoritySettingProject");
        }
        return attributes;
    }

    public void onClickSaveButton(View view){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.ADD_PROJECT_ROLE)
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.NAME, roleNameEditText.getText().toString())
                .add(JsonUtil.Key.DATA, getAttributes()));
        finish();
    }
}
