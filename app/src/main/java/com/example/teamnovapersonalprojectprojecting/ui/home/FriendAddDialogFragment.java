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
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
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
                ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.FRIENDS.getPath("addWaiting.php"))
                        .add("waitingUserName", waitingUserName)
                        .add("userId", DataManager.Instance().userId);
                serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback() {
                    @Override
                    protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                        super.onGetJson(jsonObject);
                        final String status = jsonObject.getString("status");
                        if (status.equals("success")){
                            mainHandler.post(()->{
                                infoTextView.setText( "[" + waitingUserName + "] 에게 친구요청을 보냈습니다.");
                                infoTextView.setVisibility(View.VISIBLE);
                                infoTextView.setTextColor(Color.BLACK);
                            });

                            final String message = jsonObject.getString("data");
                            ServerConnectManager.Log(status);
                            ServerConnectManager.Log(message);
                        } else {
                            mainHandler.post(()-> {
                                infoTextView.setText("[" + waitingUserName + "] 에게 친구요청에 실패 했습니다.");
                                infoTextView.setVisibility(View.VISIBLE);
                                infoTextView.setTextColor(Color.RED);
                            });
                        }
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
