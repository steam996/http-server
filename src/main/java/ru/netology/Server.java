package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server {


    public void listen(int port) {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while (true){
                var socket = serverSocket.accept();
                threadPool.submit(() -> new ConnectionClient(socket).run());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
