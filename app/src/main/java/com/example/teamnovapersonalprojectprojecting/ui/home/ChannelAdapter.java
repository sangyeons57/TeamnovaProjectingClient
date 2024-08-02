package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.ChatActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.SearchActivity;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.MyViewHolder> {

    private List<MyItem> itemList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewContent;
        public LinearLayout expandableLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewContent = itemView.findViewById(R.id.textview_content);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
        }
    }

    public ChannelAdapter(List<MyItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channellist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MyItem item = itemList.get(position);
        holder.textViewTitle.setText(item.getTitle());

        holder.expandableLayout.removeAllViews();
        for (String content: item.getContent()) {
            LayoutInflater inflater = LayoutInflater.from(holder.expandableLayout.getContext());
            View itemView = inflater.inflate(R.layout.item_channel, holder.expandableLayout, false);

            TextView itemText = itemView.findViewById(R.id.textview_content);
            itemText.setText(content);
            itemText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    v.getContext().startActivity(intent);
                }
            });

            holder.expandableLayout.addView(itemView);
        }


        boolean isExpanded = item.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.textViewTitle.setOnClickListener(v -> {
            item.setExpanded(!item.isExpanded());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class MyItem {
        private String title;
        private List<String> content;
        private boolean expanded;

        public MyItem(String title, List<String> content) {
            this.title = title;
            this.content = content;
            this.expanded = false;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getContent() {
            return content;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }
    }
}

