package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;

import java.util.List;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.MyViewHolder> {

    private List<MyItem> itemList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.server_list_item);
        }
    }

    public ServerListAdapter(List<MyItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_projectlist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MyItem item = itemList.get(position);
        holder.imageView.setImageResource(item.getImage());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class MyItem {
        private int image;

        public MyItem(int image) {
            this.image = image;
        }

        public int getImage() {
            return image;
        }
    }
}
