package com.example.teamnovapersonalprojectprojecting.chat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.DB_ChatTable;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.FileSocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.SendMessage;
import com.example.teamnovapersonalprojectprojecting.util.ChatEditor;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.Retry;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatActivity extends AppCompatActivity implements EditChatDialogFragment.OnEditChatDataButtonClickListener {

    public enum SendType {
        ADD,
        EDIT,
    }
    private static final int pageSize = 20;

    public static final String LAST_CHAT_ID = "lastChatId";
    public static final String IS_DM = "isDM";

    private RecyclerView chatRecyclerView;
    private LinearLayoutManager layoutManager;
    private List<ChatAdapter.ChatItem> chatList;
    private ChatAdapter adapter;

    private ImageButton sendButton;
    private ImageButton addMultiMediaButton;
    private EditText messageEditText;
    private TextView channelNameTextView;
    private TextView explainTextView;

    private SocketEventListener.EventListener sendMessageEventListener;
    private SocketEventListener.EventListener removeChatDataEventListener;
    private SocketEventListener.EventListener editChatDataEventListener;

    private boolean isAtBottom;
    private boolean isReadyToLoadMoreData;

    private SendType sendType;
    private ImageGallery imageGallery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        DataManager.Instance().currentContext = this;

        isAtBottom = true;
        isReadyToLoadMoreData = true;
        sendType = SendType.ADD;

        sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);
        channelNameTextView = findViewById(R.id.userNameTextView);
        addMultiMediaButton = findViewById(R.id.multiMediaButton);
        explainTextView = findViewById(R.id.explainTextView);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        chatRecyclerView.setLayoutManager(layoutManager);

        imageGallery = new ImageGallery(this, findViewById(R.id.imagePickLayout));

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, getSupportFragmentManager()); // Create your adapter with chat data
        chatRecyclerView.setAdapter(adapter);

        messageEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if(hasFocus){
                imageGallery.setActive(false);
            }
        });

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(oldBottom - bottom > 100){
                    chatRecyclerView.smoothScrollToPosition(0);
                }
            }
        });

        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (isReadyToLoadMoreData && newState == RecyclerView.OVER_SCROLL_ALWAYS && !isAtBottom) {
                    loadMoreData(true, false);
                }
            }
        });
        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //화면이 올라갈떄 (과거 데이터를 볼떄)
                if (layoutManager != null && dy < 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (isReadyToLoadMoreData && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
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

        Intent intent = getIntent();
        int lastChatId = intent.getIntExtra(LAST_CHAT_ID, 0);
        boolean isDM = intent.getBooleanExtra(IS_DM, true);
        int channelId = DataManager.Instance().channelId;

        Log.d("ChatActivity", "NewLastChatId: " + lastChatId + " BeforeLastChatId: " + LocalDBChat.GetTable(DB_ChatTable.class).getLastChatId(channelId));

        if(lastChatId == LocalDBChat.GetTable(DB_ChatTable.class).getLastChatId(channelId)){
            loadMoreData(false, true);
        } else {
            loadMoreDataFromServer(channelId, 20, 0 , false, true);
        }

        //체팅방 이름 설정부분
        LocalDBMain.GetTable(DB_ChannelList.class).getChannelDataByServer(DataManager.Instance().channelId);
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_CHANNEL_DATA, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_CHANNEL_DATA){
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {
                DataManager.Instance().mainHandler.post(()->{
                    if(isDM){
                        new Retry(()->{
                            try {
                                int otherId = LocalDBMain.GetTable(DB_DMList.class).getOtherId(DataManager.Instance().channelId);
                                String title = LocalDBMain.GetTable(DB_UserList.class).getUsername(otherId);
                                channelNameTextView.setText(title);
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                                return false;
                            }
                            return true;
                        }).setMaxRetries(5).setRetryInterval(1000).execute();

                    } else {
                        channelNameTextView.setText(jsonUtil.getString(JsonUtil.Key.CHANNEL_NAME, DataManager.NOT_SETUP_S));
                    }
                });
                return false;
            }
        });

        addMultiMediaButton.setOnClickListener((v)->{
            imageGallery.changeActiveState();
        });

        sendButton.setOnClickListener((v)->{
            switch (sendType){
                case ADD:
                    addChatData();
                case EDIT:
                    editChatData();
                    hideEditMode();
            }
        });

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.SEND_MESSAGE, sendMessageEventListener = this::sendMessageEvent);
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.REMOVE_CHAT_DATA, removeChatDataEventListener = this::removeChatDataEvent);
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.EDIT_CHAT_MESSAGE, editChatDataEventListener = this::editChatDataEvent);

        hideEditMode();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.SEND_MESSAGE, sendMessageEventListener);
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.REMOVE_CHAT_DATA, removeChatDataEventListener);
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.EDIT_CHAT_MESSAGE, editChatDataEventListener);
        DataManager.Instance().channelId = DataManager.NOT_SETUP_I;

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EXIT_CHANNEL));
    }

    private int editChatId;
    @Override
    public void onEditChatDataButtonClick(int chatId) {
        sendType = SendType.EDIT;
        this.editChatId = chatId;
        explainTextView.setVisibility(View.VISIBLE);
        messageEditText.setText(ChatEditor.CreateChatEditor(getChatItemByChatId(chatId).message).getMessage());

        messageEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);

        explainTextView.setText("수정 취소");
        explainTextView.setOnClickListener((view)->{
            hideEditMode();
        });
    }
    private void hideEditMode(){
        sendType = SendType.ADD;
        messageEditText.setText("");
        explainTextView.setVisibility(View.GONE);
        editChatId = DataManager.NOT_SETUP_I;
    }

    private boolean sendMessageEvent(JsonUtil jsonUtil){
        int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, 0);
        int writerId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
        String message = jsonUtil.getString(JsonUtil.Key.MESSAGE, "");
        String lastTime = jsonUtil.getString(JsonUtil.Key.DATETIME, "");
        boolean isModified = jsonUtil.getBoolean(JsonUtil.Key.IS_MODIFIED, false);
        long id = SendMessage.lastChatId;

        if(jsonUtil.getBoolean(JsonUtil.Key.IS_SELF, false)) {
            editNotReadyChatItem(id, writerId, chatId, isModified, message, jsonUtil.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00"));
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
    }

    private boolean editChatDataEvent(JsonUtil jsonUtil){
        int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, DataManager.NOT_SETUP_I);
        String message = jsonUtil.getString(JsonUtil.Key.MESSAGE, DataManager.NOT_SETUP_S);
        DataManager.Instance().mainHandler.post(()->adapter.notifyItemChanged(adapter.editChat(chatId, message)));
        return false;
    }

    private boolean removeChatDataEvent(JsonUtil jsonUtil){
        int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, DataManager.NOT_SETUP_I);
        DataManager.Instance().mainHandler.post(()->{
            adapter.notifyItemRemoved(adapter.removeChat(chatId));
        });
        return false;
    }
    private void editNotReadyChatItem(long id, int writerId, int chatId, boolean isModified, String message, String dateTime){
        ChatAdapter.ChatItem chatItem = adapter.pollNotReadyChat();
        if (chatItem != null) {
            chatItem.dateTime = dateTime;
            chatItem.message = message;
            chatItem.isModified = isModified;
            chatItem.chatId = chatId;
            chatItem.userId = writerId;
            chatItem.id = (int) id;
            runOnUiThread(() -> {
                adapter.notifyItemChanged(0);
                chatRecyclerView.smoothScrollToPosition(0);
            });
            adapter.changeChatItemMapId(chatId, chatItem);
        }
    }
    public ChatAdapter.ChatItem getChatItemByChatId(int chatId){
        return chatList.stream().filter(item -> item.chatId == chatId).findFirst().get();
    }
    private void loadMoreData(boolean isNotifyData, boolean isScrollToBottom) {
        int channelId = DataManager.Instance().channelId;
        int offset = chatList.size();
        int limit = chatList.size() % pageSize == 0 ? pageSize :  pageSize - chatList.size() % pageSize;
        Log.d("ChatActivity", "limit: " + limit + ", offset: " + offset);


        LocalDBChat.GetTable(DB_ChatTable.class).getChatDataRangeFromBack(channelId, limit, offset).execute(new CursorReturn.Execute() {
            @Override
            public void run(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    List<ChatAdapter.ChatItem> chatItemList = cursorToChatItem(cursor);

                    JSONArray messageArray = new JSONArray(
                            chatItemList.stream()
                                    .map(chatItem -> ChatEditor.CreateChatEditor(chatItem.message)
                                            .setChatId(chatItem.chatId)
                                            .jsonDataToString())
                                    .collect(Collectors.toList())
                    );
                    SocketConnection.sendMessage(new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.CHECK_MESSAGE_UPDATE.toString())
                            .add(JsonUtil.Key.DATA, messageArray));

                    SocketEventListener.addAddEventQueue(SocketEventListener.eType.CHECK_MESSAGE_UPDATE, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.CHECK_MESSAGE_UPDATE){
                        @Override
                        public boolean runOnce(JsonUtil jsonUtil) {
                            if(jsonUtil.getBoolean(JsonUtil.Key.IS_VALID, false)){
                                for (ChatAdapter.ChatItem element: chatItemList) {
                                    adapter.addChat(element);
                                }

                                chatRecyclerView.post(()->{
                                    if(isNotifyData) {
                                        adapter.notifyItemRangeInserted(offset, cursor.getCount());
                                    }
                                    if(isScrollToBottom){
                                        chatRecyclerView.scrollToPosition(0);
                                    }
                                });
                                isReadyToLoadMoreData = true;
                            } else {
                                loadMoreDataFromServer(channelId, limit, offset, isNotifyData, isScrollToBottom );
                            }
                            return false;
                        }
                    });
                } else {
                    loadMoreDataFromServer(channelId, limit, offset, isNotifyData, isScrollToBottom);
                }
            }
        });
    }

    private void addChatData(){
        Log.d("addChatData", "message: " + messageEditText.getText().toString().trim() + " " + imageGallery.selectedPath.size() );
        if(messageEditText.getText().toString().trim().equals("") && imageGallery.selectedPath.isEmpty()) {
            return ;
        }

        ChatEditor chatEditor = ChatEditor.CreateChatEditor(messageEditText.getText().toString());
        //전송하려는 파일이 존제한다면
        if(!imageGallery.selectedPath.isEmpty()){
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.FILE, new SocketEventListener.EventListener() {
                int countFile = 0;
                @Override
                public boolean run(JsonUtil jsonUtil) {
                    if(jsonUtil.getBoolean(JsonUtil.Key.IS_VALID, false)){
                        int fileId = jsonUtil.getInt(JsonUtil.Key.ID, DataManager.NOT_SETUP_I);
                        Log.d("addChatData", "fileId: " + fileId);
                        chatEditor.addFileId(fileId);

                        if(++countFile >= imageGallery.selectedPath.size()){
                            SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.FILE, this);
                            DataManager.Instance().mainHandler.post(()->{ addChatData(chatEditor); });
                            imageGallery.selectedPath.clear();
                        }
                    }
                    return false;
                }
            });
            for (Uri path: imageGallery.selectedPath) {
                Log.d("addChatData", path.toString());
                FileSocketConnection.sendFile(path);
                //file은 2번 메시지 보넴
            }
        } else {
            addChatData(chatEditor);
        }
    }
    private void addChatData(ChatEditor chatEditor) {
        adapter.addNotReadyChat(0, new ChatAdapter.ChatItem(
                DataManager.NOT_SETUP_I,
                DataManager.NOT_SETUP_I,
                DataManager.Instance().userId,
                chatEditor.jsonDataToString(),
                "",
                false
        ));

        adapter.notifyItemInserted(0);
        chatRecyclerView.scrollToPosition(0);

        SocketConnection.sendMessage(false, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SEND_MESSAGE)
                .add(JsonUtil.Key.MESSAGE, chatEditor.jsonDataToString())
                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                .add(JsonUtil.Key.USERNAME, DataManager.Instance().username));

        messageEditText.setText("");
        messageEditText.clearFocus();
        imageGallery.setActive(false);
    }

    private List<ChatAdapter.ChatItem> cursorToChatItem(Cursor cursor){
        List<ChatAdapter.ChatItem> chatItemList = new ArrayList<>();
        do{
            int id = cursor.getInt(0);
            int chatId = cursor.getInt(2);
            int writerId = cursor.getInt(3);
            String data = cursor.getString(4);
            String lastTime = cursor.getString(5);
            boolean isModified = cursor.getInt(6) == 1;

            chatItemList.add( new ChatAdapter.ChatItem(
                    id,
                    chatId,
                    writerId,
                    data,
                    lastTime,
                    isModified
            ));
        } while (cursor.moveToNext());

        return chatItemList;
    }
    private void loadMoreDataFromServer(int channelId, int limit, int offset, boolean isNotifyData, boolean isScrollToBottom){
        isReadyToLoadMoreData = false;
        LocalDBChat.GetTable(DB_ChatTable.class).addOrUpdateChatByServer(channelId, limit, offset);
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_CHAT_DATA, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_CHAT_DATA){
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {

                LocalDBChat.GetTable(DB_ChatTable.class).getChatDataRangeFromBack(channelId, limit, offset).execute(new CursorReturn.Execute() {
                    @Override
                    public void run(Cursor cursor) {
                        Log.d("ChatActivity", "cursor count: " + cursor.getCount());
                        if (cursor.moveToFirst()) {
                            List<ChatAdapter.ChatItem> chatItemList = cursorToChatItem(cursor);
                            for (ChatAdapter.ChatItem element : chatItemList) {
                                adapter.addChat(element);
                            }
                            chatRecyclerView.post(() -> {
                                if (isNotifyData) {
                                    adapter.notifyItemRangeInserted(offset, cursor.getCount());
                                }
                                if (isScrollToBottom) {
                                    chatRecyclerView.scrollToPosition(0);
                                }
                            });
                            isReadyToLoadMoreData = true;
                        } else {
                            isReadyToLoadMoreData = false;
                        }
                    }
                });
                return false;
            }
        });
    }
    private void editChatData(){
        Log.d("chatItem", "c: "+editChatId + " " + messageEditText.getText().toString());
        if(editChatId != DataManager.NOT_SETUP_I) {
            LocalDBChat.GetTable(DB_ChatTable.class).getChatData(DataManager.Instance().channelId, editChatId).execute((cursor)->{
                Log.d("chatItem", ""+cursor.moveToFirst());
                if(cursor.moveToFirst()){
                    ChatEditor chatEditor = ChatEditor.CreateChatEditor(cursor.getString(cursor.getColumnIndexOrThrow("data")));
                    chatEditor.setChatMessage(messageEditText.getText().toString());
                    Log.d("EditChatData", "editChatId: " + editChatId + " message: " + chatEditor.jsonDataToString());
                    SocketConnection.sendMessage(new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EDIT_CHAT_MESSAGE)
                            .add(JsonUtil.Key.CHAT_ID, editChatId)
                            .add(JsonUtil.Key.MESSAGE, chatEditor.jsonDataToString()));
                }
            });
        }
    }


    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    // 이미지 선택을 위한 인텐트를 시작하는 메서드
    private void openImageChooser() {
    }



}
