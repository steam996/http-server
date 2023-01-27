package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ConnectionClient {

    private Socket socket;

    public ConnectionClient(Socket socket) {
        this.socket = socket;
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
            final var parts = requestLine.split(" ");
            System.out.println(requestLine);

            if (parts.length != 3) {
                returnCode404(out);
                return;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                returnCode404(out);
                return;
            }

            returnPage(path, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void returnCode404(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void returnCode200(BufferedOutputStream out, Path filePath, long length) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
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

            returnCode200(out, filePath, content.length);
            out.write(content);
            out.flush();
        } else {
            final var length = Files.size(filePath);
            returnCode200(out, filePath, length);
            Files.copy(filePath, out);
            out.flush();
        }
    }
}
