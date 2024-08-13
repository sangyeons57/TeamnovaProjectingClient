package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import android.content.Intent;

import com.example.teamnovapersonalprojectprojecting.ChatActivity;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONObject;

public class JoinDMChannel implements SocketEventListener.EventListener {
    @Override
    public void run(JsonUtil jsonUtil) {
        String status = jsonUtil.getString(JsonUtil.Key.STATUS, "error");

        if (status.equals("success")) {
            DataManager.Instance().channelId = jsonUtil.getString(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP);

            Intent intent = new Intent(DataManager.Instance().currentContext, ChatActivity.class);
            intent.putExtra("JsonData", jsonUtil.toString());
            intent.putExtra("channelName", jsonUtil.getString(JsonUtil.Key.FRIEND_NAME, "체팅방이름 불러오기 에러"));

            DataManager.Instance().currentContext.startActivity(intent);
        } else {
            SocketEventListener.LOGe(jsonUtil.toString());
        }
    }
}
