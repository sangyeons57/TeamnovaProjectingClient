package com.example.teamnovapersonalprojectprojecting.socket;

import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.socket.eventList.JoinDMChannel;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketEventListener {
    public enum eType {
        NONE("NONE"),
        ADD_FRIEND_ON_WAITING("AddFriendOnWaiting"),
        ADD_WAITING("AddWaiting"),
        CREATE_DM_CHANNEL("CreateDMChannel"),
        EXIT_CHANNEL("ExitChannel"),
        JOIN_DM_CHANNEL("JoinDMChannel"),
        REMOVE_WAITING_DATA("RemoveWaitingData"),
        SEND_MESSAGE("SendMessage"),
        SET_USER("SetUser"),
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
        addEvent(eType.JOIN_DM_CHANNEL, new JoinDMChannel());
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

    public static void addEvent(eType event, EventListener eventListener){
        if(!Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.put(event,new ArrayList<>());
        }
        Instance().eventListMap.get(event).add(eventListener);
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

    public static void callEvent(eType event, JsonUtil jsonutil){
        LOG("CallEvent: " + event +" " + jsonutil.toString());
        if(!Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.put(event,new ArrayList<>());
        }
        Instance().eventListMap.get(event).stream()
                .forEach(eventListener -> eventListener.run(jsonutil));
    }

    public interface EventListener{
        public void run(JsonUtil jsonUtil);
    }
}
