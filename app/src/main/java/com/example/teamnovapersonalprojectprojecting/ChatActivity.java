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

import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.home.ChatAdapter;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

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

    private Intent intent;
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

        //JoinDMChannel로 부터 이전 데이터를 받아서 출력하는 부분
        /*
        TODO: 데이터 를 읽어와서 로컬 파일에 저장한 다음 로컬 파일을 읽어서 데이터를 출력하게 해야함
         또한 한번에 출력하는게아니라 나눠서 출력하게 해서 한번에 데이터 읽어오는 떄 느끼는 잔랙을 없에거나
         그래야함 즉 한 번읽은 데이터는 다시 로드하지 않도록
         하지만 데이터가 유효한 건지 검증하는 작업을 할필요 가 있음
         */
        this.intent = getIntent();
        String jsonData = intent.getStringExtra("JsonData");
        SocketConnection.LOG(jsonData);
        try {
            JsonUtil jsonUtil = new JsonUtil(jsonData);
            JSONArray jsonArray = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++){
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

                SocketConnection.sendMessage(new JsonUtil()
                                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SEND_MESSAGE)
                                .add(JsonUtil.Key.CHAT_ID, chatId)
                                .add(JsonUtil.Key.MESSAGE, messageEditText.getText().toString())
                                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                                .add(JsonUtil.Key.USERNAME, DataManager.Instance().username)
                                .add(JsonUtil.Key.CHANNEL_ID, DataManager.Instance().channelId));
                messageEditText.setText("");
                messageEditText.clearFocus();
            }
        });

        SocketEventListener.addEvent(SocketEventListener.eType.SEND_MESSAGE, (jsonUtil)->{
            if(jsonUtil.getBoolean(JsonUtil.Key.IS_SELF, false)) {
                SocketConnection.LOG(jsonUtil.toString());
                ChatAdapter.ChatItem chatItem = adapter.getChat(jsonUtil.getInt(JsonUtil.Key.CHAT_ID, -1));
                if (chatItem != null) {
                    chatItem.dateTime = jsonUtil.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00");
                    runOnUiThread(() -> { adapter.notifyItemChanged(adapter.getPosition(chatItem)); });
                }
            } else {
                addChat( new ChatAdapter.ChatItem(
                        R.drawable.ic_account_black_24dp,
                        jsonUtil.getString(JsonUtil.Key.USERNAME, "문제 발생"),
                        jsonUtil.getString(JsonUtil.Key.MESSAGE, "문제 발생"),
                        jsonUtil.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00")
                ));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.Instance().channelId = DataManager.NOT_SETUP;

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EXIT_CHANNEL));
    }
    public int addChat(ChatAdapter.ChatItem chatItem) {
        int chatId = adapter.addChat(chatItem);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(chatList.size());
            chatRecyclerView.smoothScrollToPosition(chatList.size());
        });
        return chatId;
    }
}
