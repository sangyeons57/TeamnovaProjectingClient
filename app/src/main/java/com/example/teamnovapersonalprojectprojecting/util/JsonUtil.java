package com.example.teamnovapersonalprojectprojecting.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    public enum Key {
        MESSAGE("message"),
        STATUS("status"),
        TYPE("type"),
        USER_ID("userId"),
        USER_ID1("userId1"),
        USER_ID2("userId2"),
        IS_SELF("isSelf"),
        CREATE_TIME("createTime"),
        UPDATE_TIME("updateTime"),
        CHANNEL_ID("channelId"),
        ;

        private String keyName;

        private Key(String key) {
            this.keyName = key;
        }

        @Override
        public String toString() {
            return keyName;
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

    public JsonUtil add(Key key, double value) throws JSONException {
        jsonObject.put(key.keyName, value);
        return this;
    }

    public JsonUtil add(Key key, boolean value) throws JSONException {
        jsonObject.put(key.keyName, value);
        return this;
    }

    public JsonUtil add(Key key, long value) throws JSONException {
        jsonObject.put(key.keyName, value);
        return this;
    }

    public JsonUtil add(Key key, int value) throws JSONException {
        jsonObject.put(key.keyName, value);
        return this;
    }

    public JsonUtil add(Key key, Object value) throws JSONException {
        jsonObject.put(key.keyName, value);
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

    public String getJsonString() {
        return jsonObject.toString();
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

    /*
    public boolean tryGetString(Key key, Ref<String> stringRef) {
        boolean answer = true;
        try {
            stringRef = new Ref(getString(key));
        } catch (JSONException e) { answer = false; }
        return answer;
    }
    public boolean tryGetJsonObject(Key key, Ref<JSONObject> stringRef) {
        boolean answer = true;
        try {
            stringRef = new Ref(getJsonObject(key));
        } catch (JSONException e) { answer = false; }
        return answer;
    }
    public boolean tryGetJsonArray(Key key, Ref<JSONArray> stringRef) {
        boolean answer = true;
        try {
            stringRef = new Ref(getJsonArray(key));
        } catch (JSONException e) { answer = false; }
        return answer;
    }
    public boolean tryGetBoolean(Key key, Ref<Boolean> stringRef) {
        boolean answer = true;
        try {
            stringRef = new Ref(getBoolean(key));
        } catch (JSONException e) { answer = false; }
        return answer;
    }
    public boolean tryGetDouble(Key key, Ref<Double> stringRef) {
        boolean answer = true;
        try {
            stringRef = new Ref(getDouble(key));
        } catch (JSONException e) { answer = false; }
        return answer;
    }
    public boolean tryGetInt(Key key, Ref<Integer> stringRef) {
        boolean answer = true;
        try {
            stringRef = new Ref(getInt(key));
        } catch (JSONException e) { answer = false; }
        return answer;
    }
    public boolean tryGetLong(Key key, Ref<Long> stringRef) {
        boolean answer = true;
        try {
            stringRef = new Ref(getLong(key));
        } catch (JSONException e) { answer = false; }
        return answer;
    }
    */

    public static boolean TryReadJson(String jsonString, Ref<JsonUtil> ref) {
        boolean answer = true;
        try {
            ref = new Ref(new JsonUtil(jsonString));
        } catch (JSONException e) {
            answer = false;
        }
        return answer;
    }

    public static class Ref<T>{
        public T value;
        public Ref(T value){
            this.value = value;
        }
    }
}
