package com.example.teamnovapersonalprojectprojecting.socket;


import android.content.Context;
import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketConnection {
    public static final int PORT = 5000;
    public static final String IP = "43-202-32-108";
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

    private BufferedReader in;
    private PrintWriter out;

    private SocketConnection(){
        taskQueue = new LinkedBlockingQueue<>();

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket clientSocket = new Socket(SERVER_ADDRESS, PORT);

                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

                    LOG(out.toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String message;
                                while ((message = in.readLine()) != null) {
                                    SocketConnection.LOG("SERVER Recieved", message);
                                    SocketEventListener.callEvent(new JSONObject(message));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            } catch (JSONException e) {
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
                                    out.println(message);
                                    LOG("FINISH Sent to Server", message);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                e.printStackTrace();
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

    public static void sendMessage(JsonUtil jsonUtil){
        sendMessage(jsonUtil.toString());
    }

    public static void sendMessage(JSONObject jsonObject) {
        sendMessage(jsonObject.toString());
    }

    public static void sendMessage(String message) {
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
