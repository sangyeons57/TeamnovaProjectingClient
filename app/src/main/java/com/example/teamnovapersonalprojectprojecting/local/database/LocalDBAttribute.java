package com.example.teamnovapersonalprojectprojecting.local.database;

import android.database.sqlite.SQLiteOpenHelper;

public abstract class LocalDBAttribute {
    protected SQLiteOpenHelper sqlite;
    public LocalDBAttribute(SQLiteOpenHelper sqlite){
        this.sqlite = sqlite;
    }
    public abstract String getCreateQuery();
    public abstract String getTableName();
}
