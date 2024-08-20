package com.example.teamnovapersonalprojectprojecting;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.StringCheck;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.EncryptedSharedPrefsManager;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout emailInputLayout;
    TextInputLayout passwordInputLayout;
    TextInputEditText emailInput;
    TextInputEditText passwordInput;

    CheckBox passwordCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Assuming your layout file is activity_login.xml
        DataManager.Instance().currentContext = this;

        Button loginButton = findViewById(R.id.loginButton);
        TextView findPasswordText = findViewById(R.id.findPasswordText);
        TextView joinText = findViewById(R.id.joinText);

        //로그인 입력 레이아웃
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.current_password_input_layout);
        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        passwordCheckBox = findViewById(R.id.password_checkbox);

        passwordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                if (!isValidEmail(email)) {
                    emailInputLayout.setError("이메일이 형식이 올바르지 않습니다.");
                    return;
                }
                if (!isValidPassword(password)) {
                    passwordInputLayout.setError("비밀번호가 올바르지 않습니다.");
                    return;
                }

                Login(email, password);
            }
        });

        findPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, FindPasswordActivity.class);
                startActivity(intent);
            }
        });

        joinText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });

        tryAutoLogin();
    }

    private boolean isValidEmail(String email){
        if(email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d("LoginActivity", "invalidEmail");
            return false;
        }
        Log.d("LoginActivity", "validEmail");
        emailInputLayout.setError(null);
        return true;
    }
    private boolean isValidPassword(String password){
        if (password.length() < 8) {
            return false;
        }
        if (password.length() > 100) {
            return false;
        }
        if (!StringCheck.containsUpperCase(password)) {
            return false;
        }
        if (!StringCheck.containsLowerCase(password)) {
            return false;
        }
        if (!StringCheck.containsDigit(password)) {
            return false;
        }
        if (!StringCheck.containsSpecialChar(password)) {
            return false;
        }
        passwordInputLayout.setError(null);
        return true;
    }
    private void tryAutoLogin(){
        EncryptedSharedPrefsManager.init(LoginActivity.this, EncryptedSharedPrefsManager.LOGIN);
        if(EncryptedSharedPrefsManager.hasKey("email", false) && EncryptedSharedPrefsManager.hasKey("password", false)){
            String email = EncryptedSharedPrefsManager.getString("email", "");
            String password = EncryptedSharedPrefsManager.getString("password", "");
            Login(email, password);
        }
    }

    private void Login(String email, String password){
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("Login.php"))
                .add("email", email)
                .add("password", password);

        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            protected void onResponseSuccess(Response response) throws IOException {
                super.onResponseSuccess(response);
                serverConnectManager.getPHPSession(response);
            }

            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                final String status = jsonObject.getString("status");
                final String message = jsonObject.getString("message");
                if(status.equals("success")) {
                    final int userId = jsonObject.getInt("user_id");
                    final String username = jsonObject.getString("user_name");
                    Log.d("JoinActivity", status);
                    Log.d("JoinActivity", message);
                    Log.d("JoinActivity", ""+userId);
                    DataManager.Instance().userId = userId;
                    DataManager.Instance().username = username;

                    DataManager.Instance().setFriendList();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    SocketConnection.sendMessage(new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SET_USER.toString())
                            .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId));

                    SocketEventListener.addEvent(SocketEventListener.eType.SET_USER, new SocketEventListener.EventListener() {
                        @Override
                        public boolean run(JsonUtil jsonUtil) {
                            SocketEventListener.LOG(jsonUtil.toString());
                            //DM리스트 가지고 오기
                            SocketConnection.sendMessage(new JsonUtil().add(JsonUtil.Key.TYPE, SocketEventListener.eType.RELOAD_DM_LIST.toString()));
                            SocketEventListener.addRemoveQueue(this);
                            return false;
                        }
                    });


                    EncryptedSharedPrefsManager.init(LoginActivity.this, EncryptedSharedPrefsManager.LOGIN);
                    EncryptedSharedPrefsManager.putString("email", email);
                    EncryptedSharedPrefsManager.putString("password", password);
                    finish();
                } else if(status.equals("email_error")) {
                    mainHandler.post(()->{
                        emailInputLayout.setError(message);
                    });
                } else {
                    mainHandler.post(()->{
                        passwordInputLayout.setError(message);
                    });
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}