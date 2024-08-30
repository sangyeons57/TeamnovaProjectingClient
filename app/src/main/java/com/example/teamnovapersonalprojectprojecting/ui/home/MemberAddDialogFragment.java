package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.project.EditChannelDialogFragment;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.UUID;

public class MemberAddDialogFragment extends DialogFragment {

    public static final String PROJECT_ID = "projectId";

    public static MemberAddDialogFragment Instance(int projectId){
        MemberAddDialogFragment dialogFragment = new MemberAddDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PROJECT_ID, projectId);
        dialogFragment.setArguments(bundle);
        return  dialogFragment;
    }

    private ImageButton copyLinkButton;

    private int projectId;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_add_member, container, false);
        copyLinkButton = view.findViewById(R.id.copy_link_button);

        projectId = getArguments().getInt(PROJECT_ID);

        copyLinkButton.setOnClickListener(this::onClickCopyLinkButton);
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

    public void onClickCopyLinkButton(View view){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN.toString())
                .add(JsonUtil.Key.PROJECT_ID, projectId));
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN){
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {
                String token = jsonUtil.getString(JsonUtil.Key.TOKEN, "");
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("token", getJoinLink(token));
                clipboardManager.setPrimaryClip(clip);

                DataManager.Instance().mainHandler.post(()->{
                    Toast.makeText(getContext(), "copy link", Toast.LENGTH_SHORT).show();
                });
                return false;
            }
        });
    }

    public static String getJoinLink(String token){
        return "http://" + SocketConnection.SERVER_ADDRESS + "/invite?token=" + token ;
    }
}
