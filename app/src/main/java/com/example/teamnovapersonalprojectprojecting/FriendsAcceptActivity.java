package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsAcceptActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_friends);
    }

    public static class DataAdapter extends RecyclerView.Adapter<DataViewHolder> {
        List<DataModel> dataModelList;

        public DataAdapter (List<DataModel> dataList) {
            this.dataModelList = dataList;
        }

        @NonNull
        @Override
        public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accept_friend, parent, false);
            return new DataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
            DataModel dataModel = dataModelList.get(position);
            holder.descriptionTextView.setText(dataModel.getDescription());
            holder.nameTextView.setText(dataModelList.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return dataModelList.size();
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }

    public class DataModel {
        private int id;
        private String name;
        private String description;

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}
