package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FriendsActivity extends AppCompatActivity {
    Button accepFriendButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        accepFriendButton = findViewById(R.id.acceptFriendButton);

        accepFriendButton.setOnClickListener(v -> {
            startActivity(new Intent(FriendsActivity.this, FriendsAcceptActivity.class));
        });
    }
}
