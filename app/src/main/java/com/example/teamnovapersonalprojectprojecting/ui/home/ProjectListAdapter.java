package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.activity.project.AddProjectActivity;
import com.example.teamnovapersonalprojectprojecting.activity.project.AddProjectSetNameActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetProjectData;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;

import java.util.List;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.MyViewHolder> {

    private List<MyItem> itemList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.server_list_item);
        }
    }

    public ProjectListAdapter(List<MyItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_projectlist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MyItem item = itemList.get(position);
        holder.imageView.setImageResource(item.getImage());
        holder.imageView.setOnClickListener(v -> {
            if(this.itemList.size() - 1 == position) {
                Intent intent = new Intent(DataManager.Instance().currentContext, AddProjectActivity.class);
                intent.putExtra(AddProjectSetNameActivity.IS_PRIVATE, true);
                DataManager.Instance().currentContext.startActivity(intent);
            } else {
                DataManager.Instance().projectName = item.getTitle();
                DataManager.Instance().projectId = item.getId();
                SocketConnection.sendMessage(new JsonUtil()
                        .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_PROJECT_DATA)
                        .add(JsonUtil.Key.PROJECT_ID, item.getId())
                        .add(JsonUtil.Key.DATA, new JSONArray()
                                .put(GetProjectData.ProjectName)
                                .put(GetProjectData.Structure)));
                SocketConnection.sendMessage(new JsonUtil()
                        .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_CHANNEL_PROJECT)
                        .add(JsonUtil.Key.PROJECT_ID, item.getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class MyItem {
        private String title;
        private int image;
        private int id;

        private int type;
        public MyItem(int id, String title, int image) {
            this.id = id;
            this.title = title;
            this.image = image;
        }


        public int getImage() {
            return image;
        }
        public String getTitle() {
            return title;
        }
        public int getId() {
            return id;
        }
    }
}
