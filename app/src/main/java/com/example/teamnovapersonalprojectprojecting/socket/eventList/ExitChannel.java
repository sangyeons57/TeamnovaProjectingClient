package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class ExitChannel implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        DataManager.Instance().channelId = DataManager.NOT_SETUP_I;
        return false;
    }
}
