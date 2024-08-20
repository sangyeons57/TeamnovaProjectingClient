package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;

public class GetChannelData implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0);
        jsonUtil.getString(JsonUtil.Key.CHANNEL_NAME, "");
        jsonUtil.getBoolean(JsonUtil.Key.IS_DM, false);
        jsonUtil.getJsonArray(JsonUtil.Key.MEMBERS, new JSONArray());

        return false;
    }
}
