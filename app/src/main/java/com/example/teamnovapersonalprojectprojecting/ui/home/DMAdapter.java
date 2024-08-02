package com.example.teamnovapersonalprojectprojecting.ui.home;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.ChatActivity;
import com.example.teamnovapersonalprojectprojecting.R;

import java.util.List;

public class DMAdapter extends RecyclerView.Adapter<DMAdapter.MyViewHolder> {

    private List<MyItem> itemList;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.dm_profile_name);
            imageView = itemView.findViewById(R.id.dm_profile_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    startActivity(itemView.getContext(), intent, null);
                }
            });
        }
    }
    public DMAdapter(List<MyItem> itemList){
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
        MyItem item = itemList.get(position);
        holder.imageView.setImageResource(item.getImage());
        holder.textView.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class  MyItem {
        private int image;
        private String name;

        public String getName() {
            return name;
        }

        public int getImage() {
            return image;
        }

        public MyItem(int image, String name) {
            this.image = image;
            this.name = name;
        }
    }

}
