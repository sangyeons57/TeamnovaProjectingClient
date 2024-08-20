package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.FriendsActivity;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.List;

public class FriendListAdded implements SocketEventListener.EventListener{
    @Override
    public boolean run(JsonUtil jsonUtil) {
        DataManager.Instance().friendList.add(new FriendsActivity.DataModel(jsonUtil.getString(JsonUtil.Key.USER_ID, "")));
        SocketEventListener.callEvent(SocketEventListener.eType.UPDATE_FRIEND_LIST, new JsonUtil());
        return false;
    }
}
