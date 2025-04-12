package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Schedules;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;

import okhttp3.EventListener;

public class GetScheduleData implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        JSONArray jsonArray = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());
        JSONObject date = jsonUtil.getJsonObject(JsonUtil.Key.DATETIME, new JSONObject());
        if (date.has("year") && date.has("month") && date.has("day")) {
            try {
                LocalDBMain.GetTable(DB_Schedules.class).removeSchedule(LocalDate.of(date.getInt("year"), date.getInt("month"), date.getInt("day")));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            LocalDBMain.GetTable(DB_Schedules.class).clearSchedule();
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                LocalDBMain.GetTable(DB_Schedules.class).addSchedule(jsonObject.getInt("id"), jsonObject.getInt("project_id"), jsonObject.getString("date"), new JSONObject(jsonObject.getString("data")));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
