package com.example.teamnovapersonalprojectprojecting.socket;


import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketConnection {
    public static final int PORT = 5000;
    public static final String IP = "43-203-247-194";
    public static final String REGION = ".ap-northeast-2";
    public static final String SERVER_ADDRESS = "ec2-" + IP + REGION + ".compute.amazonaws.com";
    public static final String NOT_SETUP = "NOT_SETUP";

    private static SocketConnection instance = null;
    public static SocketConnection Instance(){
        if(instance == null){
            instance = new SocketConnection();
        }
        return instance;
    }
    public static void Reset(){
        instance = null;
    }

    public static void LOG(String title, int logText){
        Log.d(SocketConnection.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String title, String logText){
        Log.d(SocketConnection.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(SocketConnection.class.getSimpleName(), logText);
    }
    public static void LOG(int logText){
        Log.d(SocketConnection.class.getSimpleName(), ""+logText);
    }
    public static void LOGe(String logText){
        Log.e(SocketConnection.class.getSimpleName(), logText);
    }
    public Thread networkThread;

    public BlockingQueue<String> taskQueue ;
    public List<SocketEventListener.eType> waitingListenList;

    private Socket clientSocket;
    private BufferedReader in;
    private DataOutputStream out;

    private SocketConnection(){
        taskQueue = new LinkedBlockingQueue<>();
        waitingListenList = new ArrayList<>();

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket(SERVER_ADDRESS, PORT);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new DataOutputStream(clientSocket.getOutputStream());

                    LOG(out.toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String message;
                                while ((message = in.readLine()) != null) {
                                    callEvent(message);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true){
                                    String message = taskQueue.take();
                                    byte[] jsonBytes = message.getBytes(StandardCharsets.UTF_8);
                                    out.writeInt(jsonBytes.length);
                                    out.write(jsonBytes);
                                    out.flush();
                                    LOG("FINISH Sent to Server", message);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                e.printStackTrace();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    LOGe(e.getMessage());
                    /**
                     * 오프라인 기능 대처하는 만들려면 이쪽에서 구현하면됨
                     */
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGe(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
        networkThread.start();
    }
    private void close(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void callEvent(String message){
        try {
            JsonUtil jsonUtil = new JsonUtil(message);
            SocketEventListener.eType type = SocketEventListener.eType.toType(jsonUtil.getString(JsonUtil.Key.TYPE, ""));
            waitingListenList.remove(type);
            LOG("SERVER RECEIVE [" + type + "]", message);
            for (SocketEventListener.eType eType: waitingListenList) {
                LOG("WAITING", eType.toString());
            }

            String status = jsonUtil.getString(JsonUtil.Key.STATUS, DataManager.NOT_SETUP_S).toLowerCase();
            if(status.contains("error") || status.contains("fail")){
                LOGe("ERROR: " + status);
                LOGe("ERROR MESSAGE: " + jsonUtil.getString(JsonUtil.Key.MESSAGE, DataManager.NOT_SETUP_S));
            }

            SocketEventListener.callEvent(type, new JsonUtil(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(JsonUtil jsonUtil) { sendMessage(true, jsonUtil); }
    public static void sendMessage(boolean waitingResponse, JsonUtil jsonUtil){
        SocketEventListener.eType type = SocketEventListener.eType.toType(jsonUtil.getString(JsonUtil.Key.TYPE, NOT_SETUP));
        if(type.equals(SocketEventListener.eType.NONE)){
            LOGe("type is not setup" + jsonUtil.toString());
            return;
        }
        sendMessage(type, jsonUtil.toString(), waitingResponse);
    }

    private static void sendMessage(SocketEventListener.eType type, String message, boolean waitingResponse) {
        if(waitingResponse && Instance().waitingListenList.contains(type)){
            LOG("ALREADY Sent to Server [" + type + "]", message);
            return;
        }
        Instance().waitingListenList.add(type);
        try {
            if (Instance().out != null) {
                LOG("START Sent to Server", message);
                Instance().taskQueue.put(message);
            } else {
                LOG("Out is null");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOG("InterruptedException", "sendMessage");
        }
    }
}
