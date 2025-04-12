package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.chat.DB_ChatTable;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class RemoveChatData implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);
        int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, DataManager.NOT_SETUP_I);
        LocalDBChat.GetTable(DB_ChatTable.class).removeChat(channelId, chatId);
        return false;
    }
}
