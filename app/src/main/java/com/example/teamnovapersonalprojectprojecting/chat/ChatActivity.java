package com.example.teamnovapersonalprojectprojecting.chat;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.DB_ChatTable;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.SendMessage;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final int pageSize = 20;

    private RecyclerView chatRecyclerView;
    private LinearLayoutManager layoutManager;
    private List<ChatAdapter.ChatItem> chatList;
    private ChatAdapter adapter;

    private ImageButton sendButton;
    private EditText messageEditText;
    private TextView channelNameTextView;

    private SocketEventListener.EventListener eventListener;

    private boolean isReadyFirstItem;
    private boolean isAtBottom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        DataManager.Instance().currentContext = this;

        isReadyFirstItem = false;
        isAtBottom = true;

        sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);
        channelNameTextView = findViewById(R.id.channelNameTextView);

        chatRecyclerView = findViewById(R.id.chatRecylerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        chatRecyclerView.setLayoutManager(layoutManager);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList); // Create your adapter with chat data
        chatRecyclerView.setAdapter(adapter);

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(oldBottom - bottom > 100){
                    chatRecyclerView.smoothScrollToPosition(0);
                }
            }
        });

        SocketEventListener.addEvent(SocketEventListener.eType.GET_LAST_CHAT_ID, new SocketEventListener.EventListener() {
            @Override
            public boolean run(JsonUtil jsonUtil) {
                int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, 0);
                int channelId = DataManager.Instance().channelId;
                if (chatId == LocalDBChat.GetTable(DB_ChatTable.class).getLastChatId(channelId)){
                    try ( Cursor cursor = LocalDBChat.GetTable(DB_ChatTable.class).getChatDataRangeFromBack(channelId, pageSize, 0); ) {
                        while (cursor.moveToNext()){
                            adapter.addChatMessages(cursor);
                        }
                        runOnUiThread(() -> {
                            chatRecyclerView.scrollToPosition(0);
                        });
                    }
                } else {
                    LocalDBChat.GetTable(DB_ChatTable.class).addOrUpdateChatByServer(channelId, pageSize, 0);
                    SocketEventListener.addEvent(SocketEventListener.eType.GET_CHAT_DATA, new SocketEventListener.EventListener(){
                        @Override
                        public boolean run(JsonUtil jsonUtil) {
                            loadMoreData(false, true);

                            SocketEventListener.addRemoveQueue(this);
                            return false;
                        }
                    });
                }

                SocketEventListener.addRemoveQueue(this);
                return false;
            }
        });
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_LAST_CHAT_ID)
                .add(JsonUtil.Key.CHANNEL_ID, DataManager.Instance().channelId));

        channelNameTextView.setText("channel name");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageEditText.getText().toString().trim().equals("")) {
                    return ;
                }

                adapter.addNotReadyChat(0, new ChatAdapter.ChatItem(
                        DataManager.NOT_SETUP_I,
                        DataManager.NOT_SETUP_I,
                        DataManager.Instance().userId,
                        messageEditText.getText().toString(),
                        "",
                        false
                ));

                adapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(0);

                SocketConnection.sendMessage(new JsonUtil()
                                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SEND_MESSAGE)
                                .add(JsonUtil.Key.MESSAGE, messageEditText.getText().toString())
                                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                                .add(JsonUtil.Key.USERNAME, DataManager.Instance().username)
                                .add(JsonUtil.Key.IS_DM, true));

                messageEditText.setText("");
                messageEditText.clearFocus();
            }
        });

         eventListener = (jsonUtil)->{
             int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, 0);
             int writerId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
             String message = jsonUtil.getString(JsonUtil.Key.MESSAGE, "");
             String lastTime = jsonUtil.getString(JsonUtil.Key.DATETIME, "");
             boolean isModified = jsonUtil.getBoolean(JsonUtil.Key.IS_MODIFIED, false);
             long id = SendMessage.lastChatId;

             if(jsonUtil.getBoolean(JsonUtil.Key.IS_SELF, false)) {
                ChatAdapter.ChatItem chatItem = adapter.pollNotReadyChat();
                if (chatItem != null) {
                    chatItem.dateTime = jsonUtil.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00");
                    chatItem.message = message;
                    chatItem.isModified = isModified;
                    chatItem.chatId = chatId;
                    chatItem.userId = writerId;
                    chatItem.id = (int) id;
                    runOnUiThread(() -> {
                        adapter.notifyItemChanged(0);
                        chatRecyclerView.smoothScrollToPosition(0);
                    });
                }
            } else {
                adapter.addChat(0, new ChatAdapter.ChatItem(
                        (int) id,
                        chatId,
                        writerId,
                        message,
                        lastTime,
                        isModified
                ));
                 runOnUiThread(() -> {
                     adapter.notifyItemInserted(0);
                     if(isAtBottom){
                         chatRecyclerView.smoothScrollToPosition(0);
                     }
                 });
            }
            return false;
        };
        SocketEventListener.addEvent(SocketEventListener.eType.SEND_MESSAGE, eventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //화면이 올라갈떄 (과거 데이터를 볼떄)
                if (layoutManager != null && dy < 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        Log.d("ChatActivity", "visibleItemCount: " + visibleItemCount + ", totalItemCount: " + totalItemCount + ", pastVisibleItems: " + pastVisibleItems);
                        // Load more data
                        loadMoreData(true, false);
                    }
                    isAtBottom = false;
                } else if (layoutManager != null && dy > 0) {
                    isAtBottom = layoutManager.findFirstVisibleItemPosition() == 0;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketEventListener.removeEvent(SocketEventListener.eType.SEND_MESSAGE, eventListener);
        DataManager.Instance().channelId = DataManager.NOT_SETUP_I;

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EXIT_CHANNEL));
    }
    private void loadMoreData(boolean isNotifyData, boolean isScrollToBottom){
        int channelId = DataManager.Instance().channelId;
        int offset = chatList.size();
        int limit = chatList.size() % pageSize == 0 ? pageSize : chatList.size() % pageSize;
        Log.d("ChatActivity", "limit: " + limit + ", offset: " + offset);

        try ( Cursor cursor = LocalDBChat.GetTable(DB_ChatTable.class).getChatDataRangeFromBack(channelId, limit, offset); ){
            if (cursor.moveToFirst()) {
                do{
                    adapter.addChatMessages(cursor);
                } while (cursor.moveToNext());
                chatRecyclerView.post(()->{
                    if(isNotifyData) {
                        adapter.notifyItemRangeInserted(offset, limit);
                    }
                    if(isScrollToBottom){
                        chatRecyclerView.scrollToPosition(0);
                    }
                });
                isReadyFirstItem = true;
            } else {
                LocalDBChat.GetTable(DB_ChatTable.class).addOrUpdateChatByServer(channelId, limit, offset);
            }
        }
    }
}
