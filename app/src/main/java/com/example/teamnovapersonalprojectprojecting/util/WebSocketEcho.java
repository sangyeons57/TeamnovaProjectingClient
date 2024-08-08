package com.example.teamnovapersonalprojectprojecting.util;

import android.content.Context;
import android.util.Log;

import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.example.teamnovapersonalprojectprojecting.LoginActivity;
import com.example.teamnovapersonalprojectprojecting.WebsocketEventListener.JoinDMChannel;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil.Key;
import com.example.teamnovapersonalprojectprojecting.util.WebsocketManager.Type;

public class WebSocketEcho extends WebSocketListener {
    public static final String IP = "43-202-32-108";
    public static final String REGION = ".ap-northeast-2";
    public static final String BASE_URL = "ws://ec2-" + IP + REGION + ".compute.amazonaws.com:8080";
    public static final String NOT_SETUP = "NOT_SETUP";

    public Context currentContext;

    private boolean isCommand = false;
    private String command;

    private Map<Type, List<OnCallListener>> eventListenerMap;

    protected WebSocket webSocket;

    private boolean isRunning = true;

    private static WebSocketEcho instance;
    private WebSocketEcho(){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "?userId=" + DataManager.Instance().userId)
                .build();

        webSocket = client.newWebSocket(request, this);
        eventListenerMap = new HashMap<>();

        this.Register();
    }

    private void Register(){
        addEventListener(Type.JOIN_DM_CHANNEL, new JoinDMChannel());
    }

    public static WebSocketEcho Instance(){
        if(DataManager.Instance().userId.equals(WebsocketManager.NOT_SETUP)){
            WebsocketManager.Loge("userId set up is first");
            return null;
        }

        if(instance == null){
            instance = new WebSocketEcho();
        }
        return instance;
    }

    public WebSocket getWebsocket(){
        return webSocket;
    }


    public void run(){
        Scanner scanner = new Scanner(System.in);
        String input;
        do{
            input = scanner.nextLine();
            if(input.trim().equals("")){
                continue;
            }

            if(isCommand){
                try {
                    commandInput(webSocket, input);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                isCommand = false;
            } else {

                if (input.equals("createDM")) {
                    isCommand = true;
                    command = "createDM";
                } else if (input.equals("setUser")) {
                    isCommand = true;
                    command = "setUser";
                } else if (input.equals("join")) {
                    isCommand = true;
                    command = "join";
                } else if (input.equals("exit")) {
                    isCommand = true;
                    command = "exit";
                } else {

                    if(!DataManager.Instance().userId.equals(WebsocketManager.NOT_SETUP) &&
                    !DataManager.Instance().channelId.equals(WebsocketManager.NOT_SETUP)){
                            WebsocketManager.Generate(webSocket).setJsonUtil(new JsonUtil()
                                    .add(Key.MESSAGE,input)
                                    .add(Key.USER_ID, DataManager.Instance().userId)
                                    .add(Key.CHANNEL_ID, DataManager.Instance().channelId))
                                    .Send(Type.MESSAGE);
                    } else {
                        System.out.println("set user or join channel is first");
                    }
                }
            }
        } while (isRunning);

        scanner.close();
        System.out.println("close");

        //client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response){
        System.out.println("Connect");

    }

    @Override
    public void onMessage(WebSocket webSocket, String text){
            try {
                WebsocketManager message = WebsocketManager.Generate(webSocket).setJsonUtil(new JsonUtil(text));;
                WebsocketManager.Log(message.getType().toString());
                callEvent(message.getType(), message);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

    @Override
    public void onMessage(WebSocket webSocket, ByteString text){
        System.out.print("MESSAGE: " + text.hex());
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        webSocket.close(1000, null);
        System.out.print("CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        t.printStackTrace();
        try {
            if(response != null){
                WebsocketManager.Loge(response.toString());
                WebsocketManager.Loge(response.body().string());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void commandInput(WebSocket webSocket, String input) throws JSONException {
        switch (command){
            case "createDM":
                WebsocketManager.Generate(webSocket).setJsonUtil(new JsonUtil()
                        .add(Key.USER_ID1, DataManager.Instance().userId)
                        .add(Key.USER_ID2, input)
                        .add(Key.USER_ID, DataManager.Instance().userId)
                        .add(Key.CHANNEL_ID, DataManager.Instance().channelId))
                        .Send(Type.CREATE_DM_CHANNEL);
                break;
            case "join":
                if(DataManager.Instance().userId.equals(WebsocketManager.NOT_SETUP)) {
                    System.out.println("UserId set fist");
                    break;
                }

                WebsocketManager.Generate(webSocket).setJsonUtil(new JsonUtil()
                        .add(Key.CHANNEL_ID,input)
                        .add(Key.USER_ID, DataManager.Instance().userId)
                        .add(Key.CHANNEL_ID, DataManager.Instance().channelId))
                        .Send(Type.JOIN_DM_CHANNEL);
                break;
            case "exit":
                if(DataManager.Instance().channelId.equals(WebsocketManager.NOT_SETUP)) {
                    System.out.println("you didn't join any channel");
                    break;
                }

                WebsocketManager.Generate(webSocket).Send(Type.EXIT_CHANNEL);
                DataManager.Instance().channelId = WebsocketManager.NOT_SETUP;
                break;
        }
    }

    public boolean addEventListener(Type type, OnCallListener listener) {
        if(eventListenerMap == null) {
            eventListenerMap = new HashMap<>();
        }
        if(type.equals(Type.NONE)|| (listener == null)){
            return false;
        }
        List<OnCallListener> listenerList;
        if(eventListenerMap.containsKey(type)){
            listenerList = eventListenerMap.get(type);
        } else {
            listenerList = new ArrayList<>();
        }

        listenerList.add(listener);

        eventListenerMap.put(type, listenerList);
        return true;
    }
    public boolean removeEventListener(Type type, OnCallListener listener) {
        if(eventListenerMap == null) {
            return false;
        }
        if(type.equals(Type.NONE)|| (listener == null)){
            return false;
        }

        if(eventListenerMap.containsKey(type)) {
            eventListenerMap.get(type).remove(listener);
        }
        return true;
    }

    public void callEvent(Type type, WebsocketManager websocketManager) {
        if(!eventListenerMap.containsKey(type)) {
            return ;
        }
        WebsocketManager.Log("call event: " + type.toString());
        WebsocketManager.Log("call event: " +websocketManager.getJsonUtil().getJsonString());

        for (OnCallListener listener: eventListenerMap.get(type))  {
            listener.onEvent(websocketManager);
        }
    }

    public interface OnCallListener {
        public void onEvent(WebsocketManager input);
    }
}
