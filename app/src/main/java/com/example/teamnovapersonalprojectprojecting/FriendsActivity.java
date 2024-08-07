package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.ui.home.FriendAddDialogFragment;
import com.example.teamnovapersonalprojectprojecting.ui.profile.ChangeStatusDialogFragment;

public class FriendsActivity extends AppCompatActivity {
    private Button acceptFriendButton;
    private ImageButton backButton;
    private TextView addFriendTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        acceptFriendButton = findViewById(R.id.acceptFriendButton);
        backButton = findViewById(R.id.backButton);
        addFriendTextView = findViewById(R.id.addFriedTextView);

        acceptFriendButton.setOnClickListener(v -> {
            startActivity(new Intent(FriendsActivity.this, FriendsAcceptActivity.class));
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        addFriendTextView.setOnClickListener(v -> {
            FriendAddDialogFragment dialogFragment = new FriendAddDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "FriendAddDialogFragment");
        });
    }
}
