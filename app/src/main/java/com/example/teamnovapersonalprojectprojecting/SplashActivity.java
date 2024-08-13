package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 2000;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = preferences.getBoolean(KEY_IS_FIRST_RUN, true);

        //NettyTest용 코드 현제는 알수없는 접근 거부 문제로 작동이 안됨
        //ServerTest.NettyTest();
        //ServerTest.AsyncHttpClientTest();
        SocketConnection.Instance();

        if(true){
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
