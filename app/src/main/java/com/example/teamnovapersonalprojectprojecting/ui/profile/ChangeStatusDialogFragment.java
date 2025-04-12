package com.example.teamnovapersonalprojectprojecting.ui.profile;

import android.app.FragmentManager;
import android.app.job.JobScheduler;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.project.ProjectJoinDialogFragment;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class ChangeStatusDialogFragment extends DialogFragment {

    public static ChangeStatusDialogFragment Instance() {
        ChangeStatusDialogFragment dialogFragment = new ChangeStatusDialogFragment();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private Button onlineButton;
    private Button noneAlarmButton;
    private Button offlineButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_change_status, container, false);

        onlineButton = view.findViewById(R.id.statusOnlineButton);
        noneAlarmButton = view.findViewById(R.id.statusNoneAlarmButton);
        offlineButton = view.findViewById(R.id.statusOfflineButton);

        highlightButton(DataManager.Instance().status);

        onlineButton.setOnClickListener((view1) -> {
            DataManager.setUserStatus(DataManager.Status.ONLINE);
            highlightButton(DataManager.Instance().status);
        });
        noneAlarmButton.setOnClickListener((view1) -> {
            DataManager.setUserStatus(DataManager.Status.NONE_ALARM);
            highlightButton(DataManager.Instance().status);
        });
        offlineButton.setOnClickListener((view1) -> {
            DataManager.setUserStatus(DataManager.Status.OFFLINE);
            highlightButton(DataManager.Instance().status);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    public void highlightButton(DataManager.Status status) {
        onlineButton.setBackgroundColor(getResources().getColor(R.color.default_app_color_bl1, null));
        noneAlarmButton.setBackgroundColor(getResources().getColor(R.color.default_app_color_bl1, null));
        offlineButton.setBackgroundColor(getResources().getColor(R.color.default_app_color_bl1, null));
        if(status == DataManager.Status.ONLINE){
            onlineButton.setBackgroundColor(getResources().getColor(R.color.default_app_color_d1, null));
        } else if (status == DataManager.Status.NONE_ALARM) {
            noneAlarmButton.setBackgroundColor(getResources().getColor(R.color.default_app_color_d1, null));
        } else if (status == DataManager.Status.OFFLINE) {
            offlineButton.setBackgroundColor(getResources().getColor(R.color.default_app_color_d1, null));
        }
    }
}
