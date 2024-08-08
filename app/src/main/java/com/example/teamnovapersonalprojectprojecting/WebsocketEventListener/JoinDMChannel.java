package com.example.teamnovapersonalprojectprojecting.WebsocketEventListener;

import android.content.Intent;
import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.ChatActivity;
import com.example.teamnovapersonalprojectprojecting.MainActivity;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.WebSocketEcho;
import com.example.teamnovapersonalprojectprojecting.util.WebsocketManager;

import org.json.JSONArray;

public class JoinDMChannel implements WebSocketEcho.OnCallListener {
    @Override
    public void onEvent(WebsocketManager input) {
        String status = input.getJsonUtil().getString(JsonUtil.Key.STATUS, "error");

        if (status.equals("success")) {
            DataManager.Instance().channelId = input.getJsonUtil().getString(JsonUtil.Key.CHANNEL_ID, WebsocketManager.NOT_SETUP);

            Intent intent = new Intent(WebSocketEcho.Instance().currentContext, ChatActivity.class);
            intent.putExtra("JsonData", input.getJsonUtil().getJsonString());
            intent.putExtra("channelName", input.getJsonUtil().getString(JsonUtil.Key.FRIEND_NAME, "체팅방이름 불러오기 에러"));

            WebSocketEcho.Instance().currentContext.startActivity(intent);
        } else {
            WebsocketManager.Loge(input.getJsonUtil().getJsonString());
        }
    }
}
