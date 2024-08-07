package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.example.teamnovapersonalprojectprojecting.util.WebSocketEcho;
import com.example.teamnovapersonalprojectprojecting.util.WebsocketManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FriendAddDialogFragment extends DialogFragment {
    TextInputEditText searchNameInput;
    Button searchNameButton;
    TextView infoTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_friend_add, container, false);
        this.searchNameInput = view.findViewById(R.id.searchNameInput);
        this.searchNameButton = view.findViewById(R.id.searchNameButton);
        this.infoTextView = view.findViewById(R.id.infoTextView);

        this.searchNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String waitingUserName = searchNameInput.getText().toString().trim();
                if(waitingUserName.equals(DataManager.Instance().username)){
                    infoTextView.setText("자기자신 에게는 친구요청을 보낼수 없습니다.");
                    infoTextView.setVisibility(View.VISIBLE);
                    infoTextView.setTextColor(Color.RED);
                    return;
                }

                WebsocketManager.Generate(WebSocketEcho.Instance().getWebsocket()).setJsonUtil(new JsonUtil()
                        .add(JsonUtil.Key.WAITING_USER_NAME, waitingUserName)
                        .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                        .add(JsonUtil.Key.USERNAME, DataManager.Instance().username))
                        .Send(WebsocketManager.Type.ADD_WAITING);

                WebSocketEcho.Instance().addEventListener(WebsocketManager.Type.ADD_WAITING, (websocketManager)->{
                    JsonUtil jsonUtil = websocketManager.getJsonUtil();
                    WebsocketManager.Log(jsonUtil.getJsonString());
                    final String status = jsonUtil.getString(JsonUtil.Key.STATUS, "error");
                    if (status.equals("success")){
                        getActivity().runOnUiThread(()-> {
                            infoTextView.setText("[" + waitingUserName + "] 에게 친구요청을 보냈습니다.");
                            infoTextView.setVisibility(View.VISIBLE);
                            infoTextView.setTextColor(Color.BLACK);
                        });

                        final String message = jsonUtil.getString(JsonUtil.Key.DATA,"data 읽기 실패");
                        ServerConnectManager.Log(status);
                        ServerConnectManager.Log(message);
                    } else {
                        getActivity().runOnUiThread(()->{
                            infoTextView.setText("[" + waitingUserName + "] 에게 친구요청에 실패 했습니다.");
                            infoTextView.setVisibility(View.VISIBLE);
                            infoTextView.setTextColor(Color.RED);
                        });
                    }
                });
            }
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
}
