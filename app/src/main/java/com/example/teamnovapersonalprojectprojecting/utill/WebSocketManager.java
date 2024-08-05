package com.example.teamnovapersonalprojectprojecting.utill;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class WebSocketManager extends WebSocketListener {
    private void run(){
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("Hello, server!");
        webSocket.send(ByteString.decodeHex("Hello, server!"));
        webSocket.close(1000, "Goodbye!");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("Received text message: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {

    }

}
