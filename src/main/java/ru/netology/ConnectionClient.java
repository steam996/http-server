package ru.netology;

import ru.netology.handlers.ResponseCodeSender;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionClient {

    private final Socket socket;
    private final ConcurrentHashMap<String,
            ConcurrentHashMap<String, Handler>> handlers;
    private final ResponseCodeSender sender = new ResponseCodeSender();

    public ConnectionClient(Socket socket, ConcurrentHashMap<String,
            ConcurrentHashMap<String, Handler>> handlers) {
        this.socket = socket;
        this.handlers = handlers;
    }


    public void run() {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
                "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
                "/classic.html", "/events.html", "/events.js");
        try (

//                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
//
            var request = Request.parse(in, out, sender);
            if (request == null) {
                sender.sendCode404(out);
                return;
            }

            if (!handlers.containsKey(request.getMethod())) {
                sender.sendCode404(out);
                return;
            }
            var pathHandlers = handlers.get(request.getMethod());
            if (!pathHandlers.containsKey(request.getPath())) {
                sender.sendCode404(out);
                return;
            }
            var handler = pathHandlers.get(request.getPath());
            try {
                handler.handle(request, out);
            } catch (Exception e) {
                sender.sendCode500(out);
                System.out.println(e.getMessage());
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
