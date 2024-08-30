package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.Retry;

public class DB_UserList extends LocalDBAttribute {
    public static final int id = 0;
    public static final int username = 1;
    public static final int profileImagePath = 2;

    public DB_UserList(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public void addUserByServer(int userId, AfterCall afterCall){
        LocalDBMain.LOG("addUserByServer: " + userId);
        SocketConnection.sendMessage(false, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_USER_DATA.toString())
                .add(JsonUtil.Key.USER_ID, userId));

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_USER_DATA, new SocketEventListener.EventListener() {
            @Override
            public boolean run(JsonUtil jsonUtil) {
                int userId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
                String username = jsonUtil.getString(JsonUtil.Key.USERNAME, "");
                LocalDBMain.LOG(DB_UserList.class.getSimpleName(), userId + " " + username);

                //이미지 다운 받고 결로 설정하는 코드가 필요함
                new Retry(()->{
                    try{
                        addOrUpdateUser(userId, username, null);
                        return true;
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        return false;
                    }
                }).setMaxRetries(5).execute();

                if(afterCall != null){
                    afterCall.execute(jsonUtil);
                }

                SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.GET_USER_DATA, this);
                return true;
            }
        });

    }

    public void addOrUpdateUser(int userId, String username, String profileImagePath){
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("id", userId);
            values.put("username", username);
            values.put("profileImagePath", profileImagePath);

            db.insertWithOnConflict(getTableName(), null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void updateAllDataByUserId(AfterCall afterCall){
        String query = "SELECT id FROM " + getTableName() + "" ;
        try(SQLiteDatabase db= this.sqlite.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, new String[]{});){
            while (cursor.moveToNext()){
                int userId = cursor.getInt(0);
                addUserByServer(userId, afterCall);
            }
        }
    }

    public String getUsername(int userId){
        try ( SQLiteDatabase db = this.sqlite.getReadableDatabase();
              Cursor cursor = db.query(getTableName(), new String[]{"username"}, "id = ?", new String[]{String.valueOf(userId)}, null, null, null);){

            if(cursor.moveToFirst()){
                return cursor.getString(0);
            }
        }
        return null;
    }

    public void changeUsername(int userId, String username){
        String sql = "UPDATE " +getTableName()+ " SET username = ? WHERE id = ?";
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase();
              SQLiteStatement stmt = db.compileStatement(sql)){

            stmt.bindString(1, username);
            stmt.bindLong(2, userId);
            stmt.executeUpdateDelete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public CursorReturn getUser(int userId){
        String query = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        return new CursorReturn(cursor, db);
    }

    public interface AfterCall {
        public void execute(JsonUtil jsonUtil);
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`id` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `username` TEXT NOT NULL," +
                "  `profileImagePath` VARCHAR(45));";
    }

    @Override
    public String getTableName() {
        return "UserList";
    }
}
