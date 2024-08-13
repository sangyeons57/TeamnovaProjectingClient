package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;

import java.util.List;
import java.util.UUID;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatItem> chatList;

    public ChatAdapter(List<ChatItem> chatList) {
        this.chatList = chatList;
    }

    public static class ChatItem {
        public int profileImage;
        public String name;
        public String message;
        public String dateTime;

        public int chatId = 0;

        public ChatItem(int profileImage, String name, String message, String dateTime) {
            this.profileImage = profileImage;
            this.name = name;
            this.message = message;
            this.dateTime = dateTime;
        }
    }

    public int addChat(ChatItem chatItem) {
        if(chatList.size() == 0){
            chatItem.chatId = 1;
        } else {
            chatItem.chatId = chatList.get(chatList.size() - 1).chatId + 1;
        }
        chatList.add(chatItem);
        SocketConnection.LOG("addchat", chatItem.chatId);

        return chatItem.chatId;
    }

    public ChatItem getChat(int chatId){
        for(int i = chatList.size() - 1; i >= 0; i--){
            if(chatList.get(i).chatId == chatId){
                SocketConnection.LOG( chatId + " " + chatList.get(i).chatId);
                return chatList.get(i);
            }
        }
        return null;
    }

    public int getPosition (ChatItem chatItem){
        return chatList.indexOf(chatItem);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImageView;
        public TextView nameTextView;
        public TextView messageTextView;
        public TextView dateTextView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chatItem = chatList.get(position);
        holder.profileImageView.setImageResource(chatItem.profileImage);
        holder.nameTextView.setText(chatItem.name);
        holder.messageTextView.setText(chatItem.message);
        holder.dateTextView.setText(chatItem.dateTime);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}