package com.example.teamnovapersonalprojectprojecting.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.WebSocket;

public class WebsocketManager  {
    public final static String NOT_SETUP = "0";
    private String userId = NOT_SETUP;
    private String channelId = NOT_SETUP;
    private Type type = Type.NONE;

    private WebSocketEcho webSocketEcho;

    private String stringObject;

    private WebSocket websocket;
    private JsonUtil jsonUtil;
    private WebsocketManager(WebSocket websocket){
        this.websocket = websocket;
        this.jsonUtil = new JsonUtil();
    }
    public WebsocketManager setUserid(String userId) {
        this.userId = userId;
        return this;
    }
    public WebsocketManager channelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public static WebsocketManager Generate(WebSocket websocket){
        return new WebsocketManager(websocket);
    }

    public JsonUtil getJsonUtil(){
        return this.jsonUtil;
    }
    public WebsocketManager setJsonUtil(JsonUtil jsonUtil){
        this.jsonUtil = jsonUtil;

        setType(jsonUtil);
        setUserid(jsonUtil);
        setChannelId(jsonUtil);
        return this;
    }

    public Type getType(){
        return type;
    }

    private void setType(JsonUtil jsonUtil){
        type = Type.toType(jsonUtil.getString(JsonUtil.Key.TYPE, Type.NONE.typeName));
    }
    private void setUserid(JsonUtil jsonUtil){
        userId = jsonUtil.getString(JsonUtil.Key.USER_ID, NOT_SETUP);
    }
    private void setChannelId(JsonUtil jsonUtil) {
        channelId = jsonUtil.getString(JsonUtil.Key.CHANNEL_ID, NOT_SETUP);
    }

    public void Send(Type type) throws JSONException {
        jsonUtil.add(JsonUtil.Key.TYPE, type);
        websocket.send(jsonUtil.getJsonString());
    }
    public void Send() throws JSONException {
        jsonUtil.add(JsonUtil.Key.TYPE, type);
        websocket.send(jsonUtil.getJsonString());
    }
    public WebsocketManager autoAddKey(JsonUtil.Key... autoAdds) {
        for (JsonUtil.Key key: autoAdds ) {
            try {
                autoAdd(jsonUtil, key);
            } catch (JSONException e) {
                Loge("autoadd Error: " + key.toString());
            }
        }

        return this;
    }
    private void autoAdd(JsonUtil jsonUtil, JsonUtil.Key key) throws JSONException {
        switch (key){
            case USER_ID:
                jsonUtil.add(JsonUtil.Key.USER_ID, DataManager.Instance().userId);
                break;
            case CHANNEL_ID:
                jsonUtil.add(JsonUtil.Key.CHANNEL_ID, DataManager.Instance().channelId);
                break;

        }
    }

    public static void Log(String message){
        Log.d("WebSocketManager", message);
    }
    public static void Loge(String message){
        Log.e("WebSocketManager", message);
    }

    public static class WebsocketMessage{
        public JsonUtil jsonUtil;
        public WebSocket websocket;
        private WebsocketMessage(WebSocket websocket, JsonUtil jsonUtil){
            this.jsonUtil = jsonUtil;
            this.websocket = websocket;
        }
        public void Send() throws JSONException {
            websocket.send(jsonUtil.Build());
        }
    }

    // public static abstract class


    public enum Type{
        NONE("None"),
        MESSAGE("Message"),
        SET_USER("SetUser"),
        CREATE_DM_CHANNEL("CreateDMChannel"),
        JOIN_CHANNEL("JoinChannel"),
        EXIT_CHANNEL("ExitChannel"),
        NOT_JSON("NotJson"),
        ;

        public static String type = "type";
        private String typeName;
        private Type(String key){
            this.typeName = key;
        }

        public static Type toType(String str){
            for (Type type: Type.values()) {
                if(type.typeName.equals(str)){
                    return type;
                }
            }
            return Type.NONE;
        }

        @Override
        public String toString() {
            return typeName;
        }
    }
}
