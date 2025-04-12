package com.example.teamnovapersonalprojectprojecting.util;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatEditor {
    public enum ChatDataKey {
        IsModified,
        IsRemoved,
        UpdateCount,
        Message,
        ChatId,
        File,
    }
    public static class ChatData {
        public int chatId;
        public String message;
        public int updateCount;
        public boolean isModified;
        public boolean isRemoved;
        public JSONArray fileIdList;

        public ChatData(JSONObject jsonObject) throws JSONException {
            this.chatId = jsonObject.has(ChatDataKey.ChatId.toString()) ? jsonObject.getInt(ChatDataKey.ChatId.toString()) : DataManager.NOT_SETUP_I;
            this.message = jsonObject.has(ChatDataKey.Message.toString()) ? jsonObject.getString(ChatDataKey.Message.toString()) : DataManager.NOT_SETUP_S;
            this.updateCount = jsonObject.has(ChatDataKey.ChatId.toString()) ? jsonObject.getInt(ChatDataKey.UpdateCount.toString()) : DataManager.NOT_SETUP_I;
            this.isModified = jsonObject.has(ChatDataKey.IsModified.toString()) && jsonObject.getBoolean(ChatDataKey.IsModified.toString());
            this.isRemoved = jsonObject.has(ChatDataKey.IsRemoved.toString()) && jsonObject.getBoolean(ChatDataKey.IsRemoved.toString());
            this.fileIdList = jsonObject.has(ChatDataKey.File.toString()) ? jsonObject.getJSONArray(ChatDataKey.File.toString()) : new JSONArray();
        }
        public JSONObject toJson() throws JSONException {
            return new JSONObject()
                    .put(ChatDataKey.ChatId.toString(), chatId)
                    .put(ChatDataKey.Message.toString(), message)
                    .put(ChatDataKey.UpdateCount.toString(), updateCount)
                    .put(ChatDataKey.IsModified.toString(), isModified)
                    .put(ChatDataKey.IsRemoved.toString(), isRemoved)
                    .put(ChatDataKey.File.toString(), fileIdList);
        }
    }

    private ChatData chatData;
    private int chatId;
    private ChatEditor(String data){
        try {
            this.chatData = new ChatData(new JSONObject(data));
        } catch (JSONException e){
            try {
                this.chatData = new ChatData(new JSONObject().put(ChatDataKey.Message.toString(), data));
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static ChatEditor CreateChatEditor(String data){
        return new ChatEditor(data);
    }
    public List<Integer> getFileIdList(){
        List<Integer> result = new ArrayList<>();
        try {
            for (int i = 0; i < chatData.fileIdList.length(); i++ ){
                result.add(chatData.fileIdList.getInt(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    public String getMessage(){
        return chatData.message;
    }
    public boolean isModified(){
        return chatData.isModified;
    }
    public String jsonDataToString(){
        try {
            return chatData.toJson().toString();
        } catch (JSONException e) {
            return DataManager.NOT_SETUP_S;
        }
    }

    public ChatEditor setChatId(int chatId) {
        this.chatId = chatId;
        this.chatData.chatId = chatId;
        return this;
    }

    public ChatEditor setChatMessage(String message) {
        this.chatData.message = message;
        return this;
    }
    public ChatEditor addFileId(int fileId) {
        this.chatData.fileIdList.put(fileId);
        return this;
    }
}
