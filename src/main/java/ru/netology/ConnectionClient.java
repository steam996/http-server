package ru.netology;

import ru.netology.handlers.ResponseCodeSender;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            System.out.println(Thread.currentThread().getName());
            
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            
            var request = Request.parse(in, out, sender);

            if (!handlers.containsKey(request.getMethod())){
                sender.sendCode404(out);
                return;
            }
            var pathHandlers = handlers.get(request.getMethod());
            if (!pathHandlers.containsKey(request.getPath())){
                sender.sendCode404(out);
                return;
            }
            var handler = pathHandlers.get(request.getPath());
            try {
                handler.handle(request, out);
            } catch (Exception e){
                sender.sendCode500(out);
                System.out.println(e.getMessage());
            }


            final var path = request.getPath();

            if (!validPaths.contains(path)) {
                sender.sendCode404(out);
                return;
            }

            returnPage(path, out);
        } catch (NullPointerException e){
            return;
        } catch (IOException e) {
            e.printStackTrace();
        
        }
    }

    private void returnPage(String path, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", path);
        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();

            sender.sendCode200(out, filePath, content.length);
            out.write(content);
            out.flush();
        } else {
            final var length = Files.size(filePath);
            sender.sendCode200(out, filePath, length);
            Files.copy(filePath, out);
            out.flush();
        }
    }
}
