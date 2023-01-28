package ru.netology;

import ru.netology.handlers.RequestCollector;
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
    private ResponseCodeSender sender = new ResponseCodeSender();
    private RequestCollector requestHandler = new RequestCollector();

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
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            System.out.println(Thread.currentThread().getName());
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            Request request = requestHandler.handle(requestLine, out, sender);

            if (!handlers.containsKey(request.getMethod())){
                sender.returnCode404(out);
                return;
            }
            var pathHeandlers = handlers.get(request.getMethod());
            if (!pathHeandlers.containsKey(request.getPath())){
                sender.returnCode404(out);
                return;
            }
            pathHeandlers.get(request.getPath()).handle(request, out);

            final var path = request.getPath();

            if (!validPaths.contains(path)) {
                sender.returnCode404(out);
                return;
            }

            returnPage(path, out);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            return;
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

            sender.returnCode200(out, filePath, content.length);
            out.write(content);
            out.flush();
        } else {
            final var length = Files.size(filePath);
            sender.returnCode200(out, filePath, length);
            Files.copy(filePath, out);
            out.flush();
        }
    }
}
