package com.example.teamnovapersonalprojectprojecting.chat;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.UserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ChatProfileDialogFragment extends DialogFragment {
    public static final String USER_ID = "userId";
    public static ChatProfileDialogFragment Instance(int userId) {
        ChatProfileDialogFragment dialogFragment = new ChatProfileDialogFragment();
        Bundle args = new Bundle();
        args.putInt(USER_ID, userId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private ImageView profileImageView;
    private TextView usernameTextView;
    private TextView userStatusTextView;
    private LinearLayout userRoleLinearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_user_profile, container, false);

        int userId = getArguments().getInt(USER_ID, DataManager.NOT_SETUP_I);

        profileImageView = view.findViewById(R.id.profileImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        userStatusTextView = view.findViewById(R.id.userStatusTextView);
        userRoleLinearLayout = view.findViewById(R.id.userRoleLinearLayout);

        UserData userData = DataManager.getUserData(userId);
        if(userData.profileImagePath != null && !userData.profileImagePath.isEmpty()) {
            DB_FileList.setFileImageToCircle(profileImageView, userData.profileImagePath);
        }
        usernameTextView.setText(userData.username);
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_USER_STATUS)
                .add(JsonUtil.Key.USER_ID, userId));

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_USER_STATUS, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_USER_STATUS) {
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {
                if(jsonUtil.getBoolean(JsonUtil.Key.IS_VALID, false)){
                    DataManager.Instance().mainHandler.post(() -> {
                        userStatusTextView.setText(DataManager.Status.toType(jsonUtil.getString(JsonUtil.Key.DATA, DataManager.Status.NONE.toString())).description);
                    });
                } else {
                    DataManager.Instance().mainHandler.post(() -> {
                        userStatusTextView.setText(DataManager.Status.OFFLINE.description);
                    });
                }
                return false;
            }
        });

        if(DataManager.Instance().projectId != DataManager.NOT_SETUP_I){
            userRoleLinearLayout.setVisibility(View.VISIBLE);
            SocketConnection.sendMessage(new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_MEMBER_ROLE_DATA)
                    .add(JsonUtil.Key.PROJECT_ID, DataManager.Instance().projectId)
                    .add(JsonUtil.Key.USER_ID, userId));
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_MEMBER_ROLE_DATA, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_MEMBER_ROLE_DATA) {
                @Override
                public boolean runOnce(JsonUtil jsonUtil) {
                    JSONObject data = jsonUtil.getJsonObject(JsonUtil.Key.DATA, new JSONObject());

                    Iterator<String> keys = data.keys();

                    while (keys.hasNext()){
                        String key = keys.next();
                        try {
                            JSONObject jsonObject = data.getJSONObject(key);
                            TextView textView = new TextView(getContext());

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                            // 마진 설정 (왼쪽, 위, 오른쪽, 아래)
                            params.setMargins(20, 5, 20,5);

                            // 설정한 LayoutParams를 TextView에 적용
                            textView.setLayoutParams(params);

                            textView.setText(jsonObject.getString("RoleName"));
                            textView.setPadding(15,5,5,15);
                            textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_background));
                            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.default_app_color_d1));
                            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                            textView.setTextSize(24);
                            DataManager.Instance().mainHandler.post(()->{
                                userRoleLinearLayout.addView(textView);
                            });

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return false;
                }
            });
        } else {
            userRoleLinearLayout.setVisibility(View.GONE);
        }

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
