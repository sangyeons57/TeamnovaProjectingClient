package com.example.teamnovapersonalprojectprojecting.chat;

import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.project.ProjectJoinDialogFragment;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.util.ChatEditor;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.Retry;
import com.example.teamnovapersonalprojectprojecting.util.UserData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatItem> chatList;  //recylerview에 실제 표시되는 값들
    private Queue<ChatItem> notReadyChat;   // 서버로 부터 응답이 돌아오지 않은 CHatItem
    private Map<Integer, ChatItem> chatItemMap;  //chatId로 ChatItme을 가져올 수 있게 해둔 것

    private FragmentManager fragmentManager;


    public ChatAdapter(List<ChatItem> chatList, FragmentManager fragmentManager) {
        this.chatList = chatList;
        notReadyChat = new LinkedList<>();
        chatItemMap = new HashMap<>();
        this.fragmentManager =fragmentManager;
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

        @NonNull
        @Override
        public String toString() {
            return "id: " + id + " chatId: " + chatId + " userId: " + userId + " message: " + message + " dateTime: " + dateTime + " isModified: " + isModified;
        }
    }

    public void addChat(int index, ChatItem chatItem) {
        if(chatItem == null || chatItem.userId == DataManager.SYSTEM_ID){
            return;
        }
        Log.d("chatAdapter", chatItem.toString() );

        chatList.add(index,chatItem);
        chatItemMap.put(chatItem.chatId, chatItem);
    }
    public int removeChat(int chatId){
        ChatItem chatItem = chatItemMap.get(chatId);
        int temp = chatList.indexOf(chatItem);
        chatList.remove(chatItem);
        chatItemMap.remove(chatId);
        return temp;
    }

    public int editChat(int chatId, String message){
        ChatItem chatItem = chatItemMap.get(chatId);
        for (Map.Entry<Integer, ChatItem> entry: chatItemMap.entrySet()) {
        }
        if(chatItem != null){
            chatItem.message = message;
        }
        return chatList.indexOf(chatItem);
    }

    public void addChat(ChatItem chatItem) {
        addChat(chatList.size(), chatItem);
    }

    public void addNotReadyChat(int index ,ChatItem chatItem){
        notReadyChat.add(chatItem);
        addChat(index, chatItem);
    }

    /**
     *
     * notreadyChat 테이블에서하나 가져옴
     * notReadyChat은 사용자에게 보여주었으나 서버로 부터 보네졌다는 응답이
     * 돌아오지 않은 chat들을 모아둔 queue이다.
     */
    public ChatItem pollNotReadyChat(){
        ChatItem chatItem = notReadyChat.poll();

        return chatItem;
    }

    public void changeChatItemMapId(int newKey, ChatItem chatItem) {
        for (Map.Entry<Integer, ChatItem> entry : chatItemMap.entrySet()) {
            if (entry.getValue().equals(chatItem)) {
                int oldKey = entry.getKey();

                // 새로운 key로 value를 넣고, 기존 key는 제거
                chatItemMap.put(newKey, chatItem);
                chatItemMap.remove(oldKey);
                break; // 하나만 변경하고 끝내려면 break
            }
        }
    }

    public int getPosition (ChatItem chatItem){
        return chatList.indexOf(chatItem);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImageView;
        public TextView nameTextView;
        public TextView messageTextView;
        public TextView dateTextView;
        public TextView modifiedIndicateTextView;
        public ConstraintLayout bodyConstraintLayout;
        public LinearLayout imagePlaceLayout;

        public ChatViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            modifiedIndicateTextView = itemView.findViewById(R.id.modifiedIndicateTextView);
            bodyConstraintLayout = itemView.findViewById(R.id.bodyConstraintLayout);
            imagePlaceLayout = itemView.findViewById(R.id.imagePlaceLayout);
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
        ChatEditor chatEditor = ChatEditor.CreateChatEditor(chatItem.message);

        holder.profileImageView.setImageResource(R.drawable.ic_account_black_24dp);
        holder.messageTextView.setText(chatEditor.getMessage());
        holder.dateTextView.setText(chatItem.dateTime);

        holder.modifiedIndicateTextView.setVisibility(chatEditor.isModified() ? View.VISIBLE : View.GONE);

        UserData userData = DataManager.getUserData(chatItem.userId);
        if(userData.profileImagePath != null && !userData.profileImagePath.isEmpty()) {
            DB_FileList.setFileImageToCircle(holder.profileImageView, userData.profileImagePath);
        }
        holder.nameTextView.setText(userData.username);

        holder.bodyConstraintLayout.setOnLongClickListener((view)->{
            EditChatDialogFragment.Instance(DataManager.Instance().channelId, chatItem.chatId, chatItem.userId)
                    .show(fragmentManager, "EditChatDialogFragment");
            return false;
        });
        holder.profileImageView.setOnLongClickListener((view)->{
            ChatProfileDialogFragment.Instance(chatItem.userId)
                    .show(fragmentManager, "EditChatDialogFragment");
            return false;
        });

        //파일 존제하면 표시
        List<Integer> fileIdList = chatEditor.getFileIdList();
        holder.imagePlaceLayout.removeAllViews();
        if (fileIdList != null && !fileIdList.isEmpty()) {
            for (int fileId: fileIdList) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        200,
                        200
                );
                holder.imagePlaceLayout.addView(createImageView(position, fileId), layoutParams);
            }
        }


        setClickableUrl(holder.messageTextView, chatItem.message);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    private ImageView createImageView(int position, int imageId){

        ImageView imageView = new ImageView(DataManager.Instance().currentContext);
        new Retry(()->{
            try {
                LocalDBMain.GetTable(DB_FileList.class).checkFileExistAndCall(imageId, (jsonUtil)->{
                    LocalDBMain.GetTable(DB_FileList.class).getFileData(imageId).execute((cursor)->{
                        if(cursor.moveToFirst()) {
                            DB_FileList.setFileImage(imageView, cursor.getString(cursor.getColumnIndexOrThrow("path")));
                        }
                    });
                });
            } catch (IllegalStateException e){
                e.printStackTrace();
                return false;
            }

            return true;
        }).setMaxRetries(5).setRetryInterval(100).executeAsync();
        return imageView;
    }

    private void setClickableUrl(TextView textview, String message){
        SpannableString spannable = new SpannableString(message);
        Matcher matcher = DataManager.Instance().urlPattern.matcher(message);

        // url이 없을경우 실행 안함
        if (!matcher.find()) {
            return;
        }
        matcher.reset();
        while (matcher.find()) {
            Uri uri = Uri.parse(matcher.group());
            String path;
            if((path = uri.getPath()) != null && uri.getHost().equals(SocketConnection.SERVER_ADDRESS)){

                String token;
                if (path.equals("/invite") && (token = uri.getQueryParameter("token")) != null) {
                    setClickableSpan(spannable, matcher.start(), matcher.end(), new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            ProjectJoinDialogFragment.Instance(token)
                                    .show(fragmentManager, "ProjectJoinDialogFragment");
                        }
                    });
                }
            } else {
                setClickableSpan(spannable, matcher.start(), matcher.end(), new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri.toString()));
                        widget.getContext().startActivity(intent);
                    }
                });
            }
        }

        textview.setText(spannable);
        textview.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setClickableSpan(SpannableString spannableString, int start, int end, ClickableSpan clickableSpan) {
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}