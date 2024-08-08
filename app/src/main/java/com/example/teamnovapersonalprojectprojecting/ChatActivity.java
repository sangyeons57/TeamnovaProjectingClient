package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.ui.home.ChatAdapter;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.WebSocketEcho;
import com.example.teamnovapersonalprojectprojecting.util.WebsocketManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private List<ChatAdapter.ChatItem> chatList;
    private ChatAdapter adapter;

    private ImageButton sendButton;
    private EditText messageEditText;
    private TextView channelNameTextView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);
        channelNameTextView = findViewById(R.id.channelNameTextView);

        chatRecyclerView = findViewById(R.id.chatRecylerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList); // Create your adapter with chat data
        chatRecyclerView.setAdapter(adapter);


        Intent intent = getIntent();
        String jsonData = intent.getStringExtra("JsonData");
        WebsocketManager.Log(jsonData);
        try {
            JsonUtil jsonUtil = new JsonUtil(jsonData);
            JSONArray jsonArray = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());
            for (int i = jsonArray.length() - 1; i >= 0; i--){
                JsonUtil data = new JsonUtil(jsonArray.getJSONObject(i));
                ChatAdapter.ChatItem chatItem = new ChatAdapter.ChatItem(
                        R.drawable.ic_account_black_24dp,
                        data.getString(JsonUtil.Key.USERNAME, "문제 발생"),
                        data.getString(JsonUtil.Key.MESSAGE, "문제 발생"),
                        data.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00")
                );
                chatItem.chatId = data.getInt(JsonUtil.Key.ID, -1);
                chatList.add(chatItem);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if(chatList.size() > 0){
            adapter.notifyItemInserted(chatList.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
        }

        channelNameTextView.setText(intent.getStringExtra("channelName"));


        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    chatRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chatRecyclerView.smoothScrollToPosition(0);
                        }
                    }, 100);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageEditText.getText().toString().trim().equals("")) {
                    return ;
                }
                int chatId = addChat( new ChatAdapter.ChatItem(
                        R.drawable.ic_account_black_24dp,
                        DataManager.Instance().username,
                        messageEditText.getText().toString(),
                        ""
                ));

                WebsocketManager.Generate(WebSocketEcho.Instance().getWebsocket()).setJsonUtil(new JsonUtil()
                                .add(JsonUtil.Key.CHAT_ID, chatId)
                                .add(JsonUtil.Key.MESSAGE, messageEditText.getText().toString())
                                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                                .add(JsonUtil.Key.USERNAME, DataManager.Instance().username)
                                .add(JsonUtil.Key.CHANNEL_ID, DataManager.Instance().channelId))
                                .Send(WebsocketManager.Type.SEND_MESSAGE);
                messageEditText.setText("");
                messageEditText.clearFocus();
            }
        });

        WebSocketEcho.Instance().addEventListener(WebsocketManager.Type.SEND_MESSAGE, (websocketManager)->{
            JsonUtil data = websocketManager.getJsonUtil();
            if(data.getBoolean(JsonUtil.Key.IS_SELF, false)) {
                WebsocketManager.Log(data.getJsonString());
                ChatAdapter.ChatItem chatItem = adapter.getChat(data.getInt(JsonUtil.Key.CHAT_ID, -1));
                if (chatItem != null) {
                    chatItem.dateTime = data.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00");
                    runOnUiThread(() -> { adapter.notifyItemChanged(adapter.getPosition(chatItem)); });
                }
            } else {
                addChat( new ChatAdapter.ChatItem(
                        R.drawable.ic_account_black_24dp,
                        data.getString(JsonUtil.Key.USERNAME, "문제 발생"),
                        data.getString(JsonUtil.Key.MESSAGE, "문제 발생"),
                        data.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00")
                ));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.Instance().channelId = WebsocketManager.NOT_SETUP;
    }
    public int addChat(ChatAdapter.ChatItem chatItem) {
        int chatId = adapter.addChat(chatItem);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(chatList.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
        });
        return chatId;
    }
}
