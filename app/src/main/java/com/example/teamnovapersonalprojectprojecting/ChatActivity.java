package com.example.teamnovapersonalprojectprojecting;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.ui.home.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RecyclerView chatRecyclerView = findViewById(R.id.chat_recylerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true); // Reversed layout
        chatRecyclerView.setLayoutManager(layoutManager);

        List<ChatAdapter.ChatItem> chatList = new ArrayList<>();
        chatList.add(new ChatAdapter.ChatItem(R.drawable.ic_account_black_24dp, "사용자1", "테스트 메시지"));
        ChatAdapter adapter = new ChatAdapter(chatList); // Create your adapter with chat data
        chatRecyclerView.setAdapter(adapter);

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

    }
}
