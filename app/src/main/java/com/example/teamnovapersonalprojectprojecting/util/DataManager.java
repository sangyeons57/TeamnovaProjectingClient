package com.example.teamnovapersonalprojectprojecting.util;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.FriendsActivity;
import com.example.teamnovapersonalprojectprojecting.LoginActivity;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataManager {
    private static DataManager instance = null;
    public static DataManager Instance(){
        if(instance == null){
            instance = new DataManager();
        }
        return instance;
    }

    public static final int SYSTEM_ID = 1;
    public static final int NOT_SETUP_I = 0;
    public static final String NOT_SETUP_S = "NOT_SETUP";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public String username;
    public int userId = NOT_SETUP_I;
    public int channelId = NOT_SETUP_I;
    public String stringUserId(){
        return String.valueOf(userId);
    }

    public List<FriendsActivity.DataModel> friendList;

    public HashMap<Integer, UserData> userDataMap;

    public Context currentContext;

    private DataManager(){
         friendList = new ArrayList<>();
         userDataMap = new HashMap<>();
    }

    public void setFriendList(){
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.FRIENDS.getPath("getFriendsList.php"))
                .add(JsonUtil.Key.USER_ID, stringUserId());
        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback() {
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                if (jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success")) {
                    JSONArray jsonArray;
                    try {
                        jsonArray = jsonObject.getJSONArray("data");
                    } catch (JSONException e) {
                        jsonArray = new JSONArray();
                    }
                    Log.d("FriendsActivity", jsonArray.toString());

                    friendList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject waiting = new JSONObject(jsonArray.getString(i));
                        LocalDBMain.GetTable(DB_UserList.class).addUserByServer(waiting.getInt(JsonUtil.Key.ID.toString()), null);
                        friendList.add(new FriendsActivity.DataModel(waiting.getString(JsonUtil.Key.ID.toString())));
                    }
                } else if (jsonObject.getString(JsonUtil.Key.STATUS.toString()).equals("success_0")) {
                } else {
                    ServerConnectManager.Loge(jsonObject.getString("errorMessage"));
                }
            }
        });
    }

    public void Logout(Context context){
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("Logout.php"));
        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                if(jsonObject.getString("status").equals("success")) {
                    instance = null;
                    mainHandler.post(()->{
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        EncryptedSharedPrefsManager.init(context, EncryptedSharedPrefsManager.LOGIN);
                        EncryptedSharedPrefsManager.clearFileData();
                    });
                }
            }
        });
    }

    public static String getCurrentDateTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DataManager.DATE_FORMAT);
        return now.format(formatter);
    }

    public static UserData getUserData(int userId){
        if( Instance().userDataMap.get(userId) == null ){
            Instance().userDataMap.put(userId, new UserData(userId));

            Cursor cursor;
            if(( cursor = LocalDBMain.GetTable(DB_UserList.class).getUser(userId)) != null ){
                cursor.moveToFirst();
                Instance().userDataMap.get(userId).username = cursor.getString(DB_UserList.username);
            } else {
                LocalDBMain.GetTable(DB_UserList.class).addUserByServer(userId, (jsonUtil)->{
                    Instance().userDataMap.get(userId).username = jsonUtil.getString(JsonUtil.Key.USERNAME, "");
                });
            }
        }

        return Instance().userDataMap.get(userId);
    }
}
