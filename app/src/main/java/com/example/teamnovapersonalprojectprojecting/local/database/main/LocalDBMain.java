package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

import java.util.HashMap;
import java.util.Map;

public class LocalDBMain extends SQLiteOpenHelper {
    public static final String DB_NAME = "Main.db";
    public static final int DB_VERSION = 1;

    private Map<Class<? extends LocalDBAttribute>, LocalDBAttribute> databaseMainMap;

    private static LocalDBMain instance = null;
    public static LocalDBMain Instance(){
        if(instance == null){
            instance = new LocalDBMain(DataManager.Instance().currentContext);
        }
        return instance;
    }
    public static void LOG(String title, int logText){
        Log.d(LocalDBMain.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String title, String logText){
        Log.d(LocalDBMain.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(LocalDBMain.class.getSimpleName(), logText);
    }
    public static void LOG(int logText){
        Log.d(LocalDBMain.class.getSimpleName(), ""+logText);
    }
    public static void LOGe(String logText){
        Log.e(LocalDBMain.class.getSimpleName(), logText);
    }
    public LocalDBMain(@Nullable Context context) {
        super(context, DataManager.Instance().userId + "_" + DB_NAME, null, DB_VERSION);
        if(DataManager.Instance().userId == DataManager.NOT_SETUP_I) {
            throw new Error("userId not setup");
        }
        databaseMainMap = new HashMap<>();

        this.Register();
    }

    private void Register(){
        databaseMainMap.put(DB_DMList.class, new DB_DMList(this));
        databaseMainMap.put(DB_UserList.class, new DB_UserList(this));
        databaseMainMap.put(DB_ChannelList.class, new DB_ChannelList(this));
    }


    public static <T extends LocalDBAttribute> T GetTable(Class<T> table){
        LocalDBAttribute db = Instance().databaseMainMap.get(table);
        return table.cast(db);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for (LocalDBAttribute attribute: databaseMainMap.values()) {
            db.execSQL(attribute.getCreateQuery());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (LocalDBAttribute attribute: databaseMainMap.values()) {
            db.execSQL("DROP TABLE IF EXISTS " + attribute.getTableName());
        }
        onCreate(db);
    }
}
