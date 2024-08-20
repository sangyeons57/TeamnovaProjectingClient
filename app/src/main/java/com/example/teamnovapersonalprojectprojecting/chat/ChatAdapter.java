package com.example.teamnovapersonalprojectprojecting.chat;

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
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.UserData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatItem> chatList;
    private Queue<ChatItem> notReadyChat;
    private Map<Integer, ChatItem> chatItemMap;

    public ChatAdapter(List<ChatItem> chatList) {
        this.chatList = chatList;
        notReadyChat = new LinkedList<>();
        chatItemMap = new HashMap<>();
    }

    public static class ChatItem {
        public int id;
        public int chatId;
        public int userId;
        public String message;
        public String dateTime;
        public boolean isModified;


        public ChatItem(int id, int chatId, int userId, String message, String dateTime, Boolean isModified) {
            this.id = id;
            this.chatId = chatId;
            this.userId = userId;
            this.message = message;
            this.dateTime = dateTime;
            this.isModified = isModified;
        }

        public ChatItem setValue(ChatItem other){
            this.id = other.id;
            this.chatId = other.chatId;
            this.userId = other.userId;
            this.message = other.message;
            this.dateTime = other.dateTime;
            this.isModified = other.isModified;
            return this;
        }
    }

    public void addChat(int index, ChatItem chatItem) {
        if(chatItem == null){
            return;
        }

        if(chatItem.chatId != DataManager.NOT_SETUP_I
                && chatItemMap.containsKey(chatItem.chatId)){
            chatItemMap.get(chatItem.chatId).setValue(chatItem);
        } else {
            chatList.add(index,chatItem);
            chatItemMap.put(chatItem.chatId, chatItem);
            LocalDBChat.LOG("addchat", chatItem.chatId);
        }
    }

    public void addChat(ChatItem chatItem) {
        if(chatItem == null){
            return;
        }

        if(chatItem.chatId != DataManager.NOT_SETUP_I
                && chatItemMap.containsKey(chatItem.chatId)){
            chatItemMap.get(chatItem.chatId).setValue(chatItem);
        } else {
            chatList.add(chatItem);
            chatItemMap.put(chatItem.chatId, chatItem);
            LocalDBChat.LOG("addchat", chatItem.chatId);
        }
    }

    public void addNotReadyChat(int index ,ChatItem chatItem){
        notReadyChat.add(chatItem);
        addChat(index, chatItem);
    }

    public void addChatMessages(Cursor cursor){
        int id = cursor.getInt(0);
        int chatId = cursor.getInt(2);
        int writerId = cursor.getInt(3);
        String data = cursor.getString(4);
        String lastTime = cursor.getString(5);
        boolean isModified = (cursor.getInt(6) == 1) ? true : false;

        ChatItem chatItem = new ChatItem(
                id,
                chatId,
                writerId,
                data,
                lastTime,
                isModified
                );
        SocketConnection.LOG(id + " " + chatId + " " + writerId + " " + data + " " + lastTime + " " + isModified);
        addChat(chatItem);
    }

    public ChatItem pollNotReadyChat(){
        return notReadyChat.poll();
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
        UserData userData = DataManager.getUserData(chatItem.userId);

        holder.profileImageView.setImageResource(R.drawable.ic_account_black_24dp);
        holder.nameTextView.setText(userData.username);
        holder.messageTextView.setText(chatItem.message);
        holder.dateTextView.setText(chatItem.dateTime);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}