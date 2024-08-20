package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class DB_UserList extends LocalDBAttribute {
    public static final int userId = 0;
    public static final int username = 1;
    public static final int profileImagePath = 2;

    public DB_UserList(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public void addUserByServer(int userId, AfterCall afterCall){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_USER_DATA.toString())
                .add(JsonUtil.Key.USER_ID, userId));

        SocketEventListener.addEvent(SocketEventListener.eType.GET_USER_DATA, new SocketEventListener.EventListener() {
            @Override
            public boolean run(JsonUtil jsonUtil) {
                int userId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
                String username = jsonUtil.getString(JsonUtil.Key.USERNAME, "");
                LocalDBMain.LOG(DB_UserList.class.getSimpleName(), userId + " " + username);

                //이미지 다운 받고 결로 설정하는 코드가 필요함
                addOrUpdateUser(userId, username, null);
                SocketEventListener.addRemoveQueue(this);

                if(afterCall != null){
                    afterCall.execute(jsonUtil);
                }
                return true;
            }
        });

    }

    public void addOrUpdateUser(int userId, String username, String profileImagePath){
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("userId", userId);
            values.put("username", username);
            values.put("profileImagePath", profileImagePath);

            db.insertWithOnConflict(getTableName(), null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public String getUsername(int userId){
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        try (Cursor cursor = db.query(getTableName(), new String[]{"username"}, "userId = ?", new String[]{String.valueOf(userId)}, null, null, null);){
            if(cursor.moveToFirst()){
                return cursor.getString(username);
            }
        }
        return null;
    }

    public void changeUsername(int userId, String username){
        SQLiteDatabase db = this.sqlite.getWritableDatabase();
        String sql = "UPDATE " +getTableName()+ " SET username = ? WHERE userId = ?";
        try (SQLiteStatement stmt = db.compileStatement(sql)){
            stmt.bindString(1, username);
            stmt.bindLong(2, userId);
            stmt.executeUpdateDelete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Cursor getUser(int userId){
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        String query = "SELECT * FROM " + getTableName() + " WHERE userId = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if(cursor.moveToFirst()) {
            return cursor;
        }
        return null;
    }
    public interface AfterCall {
        public void execute(JsonUtil jsonUtil);
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`userId` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `username` VARCHAR(20) NOT NULL," +
                "  `profileImagePath` VARCHAR(45));";
    }

    @Override
    public String getTableName() {
        return "UserList";
    }
}
