package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.List;

public class DMAdapter extends RecyclerView.Adapter<DMAdapter.MyViewHolder> {

    private List<DataModel> itemList;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        int userId;
        int channelId;
        View itemView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = itemView.findViewById(R.id.dm_profile_name);
            imageView = itemView.findViewById(R.id.dm_profile_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SocketConnection.sendMessage(new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.JOIN_CHANNEL)
                            .add(JsonUtil.Key.CHANNEL_ID, channelId));
                }
            });
        }

        public void setData(DataModel dataModel){
            channelId = dataModel.getChannelId();
            textView.setText(dataModel.getName());
            userId = dataModel.getUserId();
        }
    }
    public DMAdapter(List<DataModel> itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dmlist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class DataModel {
        private int userId;
        private int channelId;
        private String name;
        public DataModel(String name, int channelId ) {
            this.name = name;
            this.channelId = channelId;
        }

        // Getters
        public int getUserId() { return userId; }
        public int getChannelId() { return channelId; }
        public String getName(){ return name; }
    }

}
