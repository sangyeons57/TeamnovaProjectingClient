package com.example.teamnovapersonalprojectprojecting.service;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.teamnovapersonalprojectprojecting.MyApp;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.LoginActivity;
import com.example.teamnovapersonalprojectprojecting.activity.MainActivity;
import com.example.teamnovapersonalprojectprojecting.activity.SplashActivity;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FriendList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.FileSocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.ChatEditor;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.EncryptedSharedPrefsManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.PKIXRevocationChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import okhttp3.Response;

public class DMAlarmForegroundService extends Service {
    public static class NotificationContent {
        public int notificationId;
        public String title;
        public List<String> messageList;
        Notification notification;
        public NotificationContent(int notificationId){
            this.notificationId = notificationId;
            messageList = new ArrayList<>();
        }
        public NotificationContent setTitle(String title){
            this.title = title;
            return this;
        }
        public NotificationContent addMessage(String message){
            messageList.add(ChatEditor.CreateChatEditor(message).getMessage());
            return this;
        }

        private Notification createNotification(Context context) {
            Intent intent = new Intent(context, SplashActivity.class) ;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            for (int i = 0; i < messageList.size(); i++){
                String message = messageList.get(i);
                style.addLine(message);
            }
            String lastMessage = messageList.get(messageList.size() - 1);
            style.setBigContentTitle(title);

            // 알림을 구성
            Notification notification =
                    new NotificationCompat.Builder(context, MESSAGE_CHANNEL)
                            .setContentTitle(title)
                            .setContentText(lastMessage)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(NotificationCompat.CATEGORY_SERVICE)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            //.setDefaults(Notification.DEFAULT_ALL)
                            //.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                            .setStyle(style)
                            //.setGroup(title)
                            //.setGroupSummary(true)
                            //.setAutoCancel(true)
                            .build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;

            // 생성한 Notification 객체 반환
            return this.notification = notification;
        }
    }
    public static final String NOT_RESTART_ACTION = "NotStartAction";
    private static final String TAG = "DMAlarmForegroundService";
    private static final int NOTIFICATION_ID = 1;

    private static final String MESSAGE_CHANNEL = "messageChannel";

    private NotificationManager notificationManager;
    private Map<Integer, NotificationContent> notificationMap;
    private Integer lastNotificationId;

    private DMAlarmForegroundService service;

    public static void Instance(Activity activity){
        Intent serverIntent = new Intent(activity, DMAlarmForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            1);
            }
        }
        activity.startForegroundService(serverIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ForegroundService", "Service Created");
        lastNotificationId = 0;
        service = this;
        notificationMap = new HashMap<>();

        createNotificationChannel();
    }
    private void createNotificationChannel(){
        NotificationChannel channel = new NotificationChannel(
                MESSAGE_CHANNEL,
                TAG + " Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(TAG + " Foreground Service Channel");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
        if(intent != null){
            if(NOT_RESTART_ACTION.equals(intent.getAction())) {
                //일반실행
                whenLoginFinish();
            } else {
                //서비스 종료로 인한 재실행
                Login();
            }
        }
         */
        startForeground(NOTIFICATION_ID, addNotification(-1, "서비스 시작", "").notification);
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.ALARM_DM, (jsonUtil)->{
            if(MyApp.isAppInForeground()){
                return false;
            }

            int userId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
            int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0);
            LocalDBMain.GetTable(DB_UserList.class).getUser(userId).execute((cursor)->{
                if(cursor.moveToFirst()){
                    addNotification(channelId, cursor.getString(1), jsonUtil.getString(JsonUtil.Key.MESSAGE, ""));
                } else {
                    addNotification(channelId, "메시지", jsonUtil.getString(JsonUtil.Key.MESSAGE, ""));
                }
            });
            return false;
        });
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.JOIN_CHANNEL, (jsonUtil) -> {
            cancelMessage(jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0));
            return false;
        });

        return START_NOT_STICKY;
    }


    private NotificationContent addNotification(int notificationId, String title, String message){
        NotificationContent notificationContent = Optional.ofNullable(notificationMap.get(notificationId)).orElse(new NotificationContent(notificationId).setTitle(title));
        notificationContent.addMessage(message);
        notificationContent.createNotification(this);
        notificationMap.put(notificationId, notificationContent);
        notificationManager.notify(notificationId, notificationContent.notification);
        return notificationContent;
    }

    // Foreground Service에서 표시할 Notification 생성
    public void cancelMessage(int notificationId){
        notificationManager.cancel(notificationId);
        notificationMap.remove(notificationId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void Login(){
        EncryptedSharedPrefsManager.init(this, EncryptedSharedPrefsManager.LOGIN);
        if(!EncryptedSharedPrefsManager.hasKey("email", false) || !EncryptedSharedPrefsManager.hasKey("password", false)) {
            return;
        }
        String email = EncryptedSharedPrefsManager.getString("email", "");
        String password = EncryptedSharedPrefsManager.getString("password", "");
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
                if(status.equals("success")) {
                    final int userId = jsonObject.getInt("user_id");
                    final String username = jsonObject.getString("user_name");
                    DataManager.Instance().userId = userId;
                    DataManager.Instance().username = username;
                    DataManager.Instance().profilePath = LocalDBMain.GetTable(DB_UserList.class).getProfileImagePath(DataManager.Instance().userId);

                    SocketConnection.sendMessage(false, new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SET_USER.toString())
                            .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId));
                    SocketEventListener.addAddEventQueue(SocketEventListener.eType.SET_USER, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.SET_USER) {
                        @Override
                        public boolean runOnce(JsonUtil jsonUtil) {
                            SocketEventListener.LOG(jsonUtil.toString());

                            //userId설정후에 FileSOckConnetion연결
                            FileSocketConnection.Instance();

                            service.whenLoginFinish();
                            return false;
                        }
                    });
                } else {
                    EncryptedSharedPrefsManager.clearFileData();
                }
            }
        });
    }
    private void whenLoginFinish(){
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.ALARM_DM,(jsonUtil)->{

            return false;
        });
    }
}
