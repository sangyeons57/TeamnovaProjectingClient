package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.List;

public class DMAdapter extends RecyclerView.Adapter<DMAdapter.MyViewHolder> {

    private List<DataModel> itemList;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        int userId;
        int channelId;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
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
            userId = dataModel.getUserId();
            Cursor cursor = null;
            try {
                cursor = LocalDBMain.GetTable(DB_UserList.class).getUser(userId);
                if(cursor ==  null){
                    LocalDBMain.GetTable(DB_UserList.class).addUserByServer(userId, jsonUtil -> {
                        textView.setText(jsonUtil.getString(JsonUtil.Key.USERNAME, ""));
                    });
                } else {
                    textView.setText(cursor.getString(DB_UserList.username));
                }

            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
            };
            channelId = dataModel.getChannelId();
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
        public DataModel(int userId, int channelId) {
            this.userId = userId;
            this.channelId = channelId;
        }

        // Getters
        public int getUserId() { return userId; }
        public int getChannelId() { return channelId; }
    }

}
