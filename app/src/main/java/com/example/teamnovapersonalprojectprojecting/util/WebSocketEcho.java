package com.example.teamnovapersonalprojectprojecting.util;

import android.service.autofill.UserData;

import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.example.teamnovapersonalprojectprojecting.util.JsonUtil.Key;
import com.example.teamnovapersonalprojectprojecting.util.WebsocketManager.Type;

public class WebSocketEcho extends WebSocketListener {
    public static final String IP = "43-202-32-108";
    public static final String REGION = ".ap-northeast-2";
    public static final String BASE_URL = "ws://ec2-" + IP + REGION + ".compute.amazonaws.com:8080/";
    public static final String NOT_SETUP = "NOT_SETUP";

    private boolean isCommand = false;
    private String command;

    protected WebSocket webSocket;

    private boolean isRunning = true;

    private static WebSocketEcho instance;
    private WebSocketEcho(){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();

        webSocket = client.newWebSocket(request, this);
    }

    public static WebSocketEcho Instance(){
        if(instance == null){
            instance = new WebSocketEcho();
        }
        return instance;
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

                    ;
                    if(!DataManager.Instance().userId.equals(WebsocketManager.NOT_SETUP) &&
                    !DataManager.Instance().channelId.equals(WebsocketManager.NOT_SETUP)){
                        try {
                            WebsocketManager.Generate(webSocket).setJsonUtil(new JsonUtil()
                                    .add(JsonUtil.Key.MESSAGE,input))
                                    .autoAddKey(JsonUtil.Key.USER_ID, JsonUtil.Key.CHANNEL_ID)
                                    .Send(Type.MESSAGE);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
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
            switch (message.getType()){
                case MESSAGE:
                    if(message.getJsonUtil().isSuccess() && message.getJsonUtil().getBoolean(JsonUtil.Key.IS_SELF, false)) {
                        System.out.print(message.getJsonUtil().getString(JsonUtil.Key.USER_ID, WebsocketManager.NOT_SETUP) + ": ");
                        System.out.println(message.getJsonUtil().getString(JsonUtil.Key.MESSAGE, NOT_SETUP));
                    } else {
                        System.out.println(message.getJsonUtil().getJsonString());
                    }
                    break;

                case SET_USER:
                    if(message.getJsonUtil().isSuccess()) {
                        System.out.println("compelete set user: " + message.getJsonUtil().getString(Key.USER_ID, WebsocketManager.NOT_SETUP));
                    } else {
                        System.out.println("fail to set user");
                        DataManager.Instance().userId = WebsocketManager.NOT_SETUP;
                    }
                    break;

                case CREATE_DM_CHANNEL:
                    if(message.getJsonUtil().isSuccess()) {
                        System.out.println("compelete create DM");
                    } else {
                        System.out.println("fail to create DM table");
                    }
                    break;
                case JOIN_CHANNEL:
                    if(message.getJsonUtil().isSuccess()) {
                        System.out.println("join channel " + message.getJsonUtil().getString(Key.CHANNEL_ID, NOT_SETUP));
                        DataManager.Instance().channelId = message.getJsonUtil().getString(Key.CHANNEL_ID, NOT_SETUP);
                    } else {
                        System.out.println("fail to join channel " + DataManager.Instance().channelId);
                    }
                }
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
        System.out.println(response.toString());
        try {
            System.out.println(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void commandInput(WebSocket webSocket, String input) throws JSONException {
        switch (command){
            case "createDM":
                WebsocketManager.Generate(webSocket).setJsonUtil(new JsonUtil()
                        .add(Key.USER_ID1, DataManager.Instance().userId)
                        .add(Key.USER_ID2, input))
                        .autoAddKey(Key.USER_ID, Key.CHANNEL_ID)
                        .Send(Type.CREATE_DM_CHANNEL);
                break;
            case "join":
                if(DataManager.Instance().userId.equals(WebsocketManager.NOT_SETUP)) {
                    System.out.println("UserId set fist");
                    break;
                }

                WebsocketManager.Generate(webSocket).setJsonUtil(new JsonUtil()
                        .add(Key.CHANNEL_ID,input))
                        .autoAddKey(Key.USER_ID)
                        .Send(Type.JOIN_CHANNEL);
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
}
