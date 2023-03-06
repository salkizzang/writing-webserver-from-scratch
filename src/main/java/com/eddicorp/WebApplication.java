package com.eddicorp;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebApplication {

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(8,16,1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(500));


        ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket;
        while ((clientSocket = serverSocket.accept()) != null) {
            ClientRequestProcessor task = new ClientRequestProcessor(clientSocket);
        }
    }
}
