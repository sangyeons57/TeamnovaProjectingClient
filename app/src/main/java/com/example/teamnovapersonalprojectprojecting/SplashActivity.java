package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 2000;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        DataManager.Instance().currentContext = this;
        boolean isFirstRun = preferences.getBoolean(KEY_IS_FIRST_RUN, true);

        SocketConnection.Instance();

        if(isFirstRun){
            setContentView(R.layout.activity_splash);
            new Handler().postDelayed(()-> {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }, SPLASH_DISPLAY_LENGTH);


            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_IS_FIRST_RUN, false);
            editor.apply();
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));

            finish();
        }
    }
}
