package com.example.teamnovapersonalprojectprojecting.util;


import android.content.Context;
import android.content.Intent;

import com.example.teamnovapersonalprojectprojecting.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DataManager {
    private static DataManager instance = null;
    public static DataManager Instance(){
        if(instance == null){
            instance = new DataManager();
        }
        return instance;
    }

    public static final String NOT_SETUP = "NOT_SETUP";

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

    public String username;
    public String userId = NOT_SETUP;
    public String channelId = NOT_SETUP;

    public Context currentContext;
}
