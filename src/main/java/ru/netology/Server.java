package ru.netology;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server {
    ConcurrentHashMap <String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public void listen(int port) {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while (true){
                var socket = serverSocket.accept();
                threadPool.submit(() -> new ConnectionClient(socket, handlers).run());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        URLEncodedUtils urlEncodedUtils = new URLEncodedUtils();

    }


    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)){
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }
}
