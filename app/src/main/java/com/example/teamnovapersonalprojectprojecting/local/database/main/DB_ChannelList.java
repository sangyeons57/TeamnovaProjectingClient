package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;

public class DB_ChannelList extends LocalDBAttribute {
    public DB_ChannelList(SQLiteOpenHelper sqlite) {
        super(sqlite);

    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`id` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `channelName` INT UNSIGNED NOT NULL UNIQUE," +
                "  `isDM` INT NOT NULL);";
    }

    @Override
    public String getTableName() {
        return "ChannelList";
    }
}
