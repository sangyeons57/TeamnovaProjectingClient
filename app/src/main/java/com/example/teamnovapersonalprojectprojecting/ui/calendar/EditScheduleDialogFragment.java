package com.example.teamnovapersonalprojectprojecting.ui.calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.Calendar24HourActivity;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Schedules;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class EditScheduleDialogFragment extends DialogFragment {
    public static final String SCHEDULE_ID = "scheduleId";

    public static EditScheduleDialogFragment Instance(int writerId) {
        EditScheduleDialogFragment dialogFragment = new EditScheduleDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SCHEDULE_ID, writerId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private Button removeChatDataButton;
    private Calendar24HourActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_edit_schedule_item, container, false);
        int scheduleId = getArguments().getInt(SCHEDULE_ID);
        activity = ((Calendar24HourActivity)getActivity());

        removeChatDataButton = view.findViewById(R.id.removeChatDataButton);

        removeChatDataButton.setOnClickListener((v)->{
            LocalDBMain.GetTable(DB_Schedules.class).removeSchedule(scheduleId);
            new Handler().postDelayed(()->{
                activity.showSchedule();
            }, 500);
            SocketConnection.sendMessage(new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.REMOVE_SCHEDULE_DATA)
                    .add(JsonUtil.Key.ID, scheduleId)
            );
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.CENTER);
        }
    }


}
