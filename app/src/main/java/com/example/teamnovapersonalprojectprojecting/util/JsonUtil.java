package com.example.teamnovapersonalprojectprojecting.util;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class JsonUtil {
    public enum Key {
        NONE("NONE"),
        MESSAGE("message"),
        STATUS("status"),
        TYPE("type"),
        ID("id"),
        CHAT_ID ("chatId"),
        USER_ID("userId"),
        USER_ID1("userId1"),
        USER_ID2("userId2"),
        USERNAME("username"),
        DATETIME("datetime"),
        DATA("data"),
        IS_SELF("isSelf"),
        CREATE_TIME("createTime"),
        UPDATE_TIME("updateTime"),
        CHANNEL_ID("channelId"),
        FRIEND_NAME("friendName"),
        WAITING_USER_NAME("waitingUserName"),
        ;

        private String keyName;

        private Key(String key) {
            this.keyName = key;
        }

        @Override
        public String toString() {
            return this.keyName;
        }

        public static Key toKey(String str) {
            for (Key key : Key.values()) {
                if (key.keyName.equalsIgnoreCase(str))
                    return key;
            }
            return Key.NONE;
        }
    }

    private JSONObject jsonObject;

    public JsonUtil() {
        jsonObject = new JSONObject();
    }

    public JsonUtil(String jsonString) throws JSONException {
        jsonObject = new JSONObject(jsonString);
    }

    public JsonUtil(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @NonNull
    @Override
    public String toString() {
        return jsonObject.toString();
    }

    public <T extends Serializable> JsonUtil add(Key key, T value) {
        try {
            jsonObject.put(key.keyName, value);
        } catch (JSONException e){
            e.printStackTrace();
            Log.e("JsonUtil", "Error occur in add key: " + key.keyName + " value: " + value);
        }
        return this;
    }
    public JsonUtil add(Key key, Object value) {
        try {
            jsonObject.put(key.keyName, value);
        } catch (JSONException e){
            e.printStackTrace();
            Log.e("JsonUtil", "Error occur in add key: " + key.keyName + " value: " + value);
        }
        return this;
    }

    public JsonUtil remove(Key key) throws JSONException {
        jsonObject.remove(key.keyName);
        return this;
    }

    public String Build() throws JSONException {
        return jsonObject.toString();
    }

    public boolean isSuccess() {
        try {
            return jsonObject.getBoolean(Key.STATUS.keyName);
        } catch (JSONException e) {
            return false;
        }
    }

    public String getString(Key key, String defaultValue) {
        try {
            return jsonObject.getString(key.keyName);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public JSONObject getJsonObject(Key key, JSONObject defaultValue) {
        try {
            return jsonObject.getJSONObject(key.keyName);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public JSONArray getJsonArray(Key key, JSONArray defaultValue) {
        try {
            return jsonObject.getJSONArray(key.keyName);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(Key key, boolean defaultValue) {
        try {
            return jsonObject.getBoolean(key.keyName);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public double getDouble(Key key, double defaultValue) {
        try {
            return jsonObject.getDouble(key.keyName);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public int getInt(Key key, int defaultValue) {
        try {
            return jsonObject.getInt(key.keyName);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public Long getLong(Key key, Long defaultValue) {
        try {
            return jsonObject.getLong(key.keyName);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}
