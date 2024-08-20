package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import android.content.Intent;

import com.example.teamnovapersonalprojectprojecting.chat.ChatActivity;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class JoinChannel implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        String status = jsonUtil.getString(JsonUtil.Key.STATUS, "error");

        if (status.equals("success")) {
            DataManager.Instance().channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);

            Intent intent = new Intent(DataManager.Instance().currentContext, ChatActivity.class);
            intent.putExtra("lastChatId", jsonUtil.getInt(JsonUtil.Key.CHAT_ID, 0));

            DataManager.Instance().currentContext.startActivity(intent);
        } else {
            SocketEventListener.LOGe(jsonUtil.toString());
        }
        return false;
    }
}
