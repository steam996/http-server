package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server {


    public void listen(int port) {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while (true){
                try{
                    ConnectionClient client = new ConnectionClient(serverSocket);
                    threadPool.submit(client);
                } catch (NullPointerException e){
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
