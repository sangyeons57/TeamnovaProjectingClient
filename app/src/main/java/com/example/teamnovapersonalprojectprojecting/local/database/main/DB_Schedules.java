package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;

import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DB_Schedules extends LocalDBAttribute {
    public DB_Schedules(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public void addSchedule(int id, int projectId, String date, JSONObject data){
        Log.d(DB_Schedules.class.getSimpleName(), "id: " + id + ", projectId: " + projectId + ", date: " + date + ", data: " + data.toString());
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("project_id", projectId);
            values.put("date", date);
            values.put("data", data.toString());

            db.insertWithOnConflict(getTableName(), null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void removeSchedule(int id){
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            db.delete(getTableName(), "id = ?", new String[]{String.valueOf(id)});
        }
    }

    public void removeSchedule(LocalDate date){
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            db.delete(getTableName(), "date = ?", new String[]{date.toString()});
        }
    }
    public void clearSchedule(){
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){
            db.execSQL("DELETE FROM " + getTableName());
        }
    }

    public CursorReturn getScheduleByMonth(LocalDate date){
        String sql = "SELECT * FROM " + getTableName() + " LIKE strftime('%Y-%m', date) = ?";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, new String[]{date.format(DateTimeFormatter.ofPattern("yyyy-MM"))});
        return new CursorReturn(cursor, db);
    }

    public CursorReturn getScheduleByDay(LocalDate date){
        Log.d(DB_Schedules.class.getSimpleName(), "date: " + date.toString());
        String sql = "SELECT * FROM " + getTableName() + " WHERE strftime('%Y-%m-%d', date) = ?";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, new String[]{date.toString()});
        return new CursorReturn(cursor, db);
    }

    public boolean isScheduleExist(LocalDate date){
        try (SQLiteDatabase db = this.sqlite.getReadableDatabase();){
            String sql = "SELECT * FROM " + getTableName() + " WHERE strftime('%Y-%m-%d', date) = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{date.toString()});
            Log.d(DB_Schedules.class.getSimpleName(),  "cursor move to first: " + cursor.moveToFirst());
            return cursor != null && cursor.moveToFirst();
        }
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`id` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `project_id` INT NOT NULL," +
                "  `date` TEXT NOT NULL," +
                "  `data` TEXT NOT NULL);";
    }

    @Override
    public String getTableName() {
        return "schedules";
    }
}
