package com.example.teamnovapersonalprojectprojecting;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class MyApp extends Application {
    private static boolean IsInForeground = false;
    private DataManager.Status lastUserStatus = DataManager.Status.NONE;

    @Override
    public void onCreate() {
        super.onCreate();
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.SET_USER, (jsonUtil)->{
            DataManager.setUserStatus(DataManager.Status.ONLINE);
            return false;
        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private int activityReferences = 0;
            private boolean isActivityChangingConfigurations = false;

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

            @Override
            public void onActivityStarted(Activity activity) {
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    // 앱이 포그라운드로 전환됨
                    IsInForeground = true;
                    DataManager.setUserStatus(lastUserStatus);
                    Log.d("Status", lastUserStatus.description);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) { }

            @Override
            public void onActivityPaused(Activity activity) { }

            @Override
            public void onActivityStopped(Activity activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations();
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    // 앱이 백그라운드로 전환됨
                    IsInForeground = false;
                    lastUserStatus = DataManager.Instance().status;
                    Log.d("Status", lastUserStatus.description);
                    DataManager.setUserStatus(DataManager.Status.OFFLINE);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

            @Override
            public void onActivityDestroyed(Activity activity) { }
        });
    }


    public static boolean isAppInForeground() {
        return IsInForeground;
    }
}
