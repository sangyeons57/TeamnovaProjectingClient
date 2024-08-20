package com.example.teamnovapersonalprojectprojecting.socket;

import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetChatData;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.CreateDMChannel;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.FriendListAdded;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetChannelData;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.JoinChannel;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.AddDMElement;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.ReloadDMList;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.SendMessage;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SocketEventListener {
    public enum eType {
        NONE("NONE"),
        ADD_DM_ELEMENT("AddDMElement"),
        ADD_FRIEND_ON_WAITING("AddFriendOnWaiting"),
        ADD_WAITING("AddWaiting"),
        CREATE_DM_CHANNEL("CreateDMChannel"),
        EXIT_CHANNEL("ExitChannel"),
        GET_CHANNEL_DATA("GetChannelData"),
        GET_CHAT_DATA("GetChatData"),
        GET_USER_DATA("GetUserData"),
        GET_LAST_CHAT_ID("GetLastChatId"),
        JOIN_CHANNEL("JoinDMChannel"),
        REMOVE_WAITING("RemoveWaiting"),
        RELOAD_DM_LIST("ReloadDMList"),
        SEND_DM_END("SendDMEnd"),
        SEND_MESSAGE("SendMessage"),
        SET_USER("SetUser"),
        UPDATE_FRIEND_LIST("UpdateFriendList"),
        ;

        private eType(String name){
            this.name = name;
        }

        private String name;

        @Override
        public String toString() {
            return name;
        }

        public static eType toType(String str){
            for (eType event: eType.values()) {
                if(event.name.equalsIgnoreCase(str))
                    return event;
            }
            return eType.NONE;
        }
    }
    private static SocketEventListener instance = null;
    public static SocketEventListener Instance(){
        if(instance == null){
            instance = new SocketEventListener();
            instance.Register();
        }
        return instance;
    }
    public void Register(){
        addEvent(eType.JOIN_CHANNEL, new JoinChannel());
        addEvent(eType.ADD_FRIEND_ON_WAITING, new FriendListAdded());
        addEvent(eType.CREATE_DM_CHANNEL, new CreateDMChannel());
        addEvent(eType.ADD_DM_ELEMENT, new AddDMElement());
        addEvent(eType.GET_CHANNEL_DATA, new GetChannelData());
        addEvent(eType.RELOAD_DM_LIST, new ReloadDMList());
        addEvent(eType.GET_CHAT_DATA, new GetChatData());
        addEvent(eType.SEND_MESSAGE, new SendMessage());
    }

    public static void LOG(String title, String logText){
        Log.d(SocketEventListener.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(SocketEventListener.class.getSimpleName(), logText);
    }
    public static void LOGe(String logText){
        Log.e(SocketEventListener.class.getSimpleName(), logText);
    }

    public Map<eType, List<EventListener>> eventListMap;
    private SocketEventListener(){
        eventListMap = new HashMap<>();

    }

    public static int addEvent(eType event, EventListener eventListener){
        if(!Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.put(event,new ArrayList<>());
        }
        Instance().eventListMap.get(event).add(eventListener);
        return Instance().eventListMap.get(event).indexOf(eventListener);
    }

    public static void removeEvent(eType event, EventListener eventListener){
        if(Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.get(event).remove(eventListener);
        }
    }
    public static void removeEvent(eType event, int position){
        if(Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.get(event).remove(position);
        }
    }

    public static void callEvent(JSONObject jsonObject){
        try {
            callEvent(eType.toType(jsonObject.getString(JsonUtil.Key.TYPE.toString())), jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void callEvent(eType event, JSONObject jsonObject) {
        callEvent(event, new JsonUtil(jsonObject));
    }

    private static Queue<EventListener> removeListenerQueue = new LinkedList<>();
    public static void addRemoveQueue(EventListener listener){
        removeListenerQueue.add(listener);
    }
    public static void callEvent(eType event, JsonUtil jsonutil){
        if(!Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.put(event,new ArrayList<>());
        }

        Instance().eventListMap.get(event).stream()
                .filter(listener -> listener.run(jsonutil))
                .findFirst();

        EventListener removeListener;
        while ((removeListener = removeListenerQueue.poll()) != null){
            removeEvent(event, removeListener);
        }
    }

    public interface EventListener{
        public boolean run(JsonUtil jsonUtil);
    }
}
