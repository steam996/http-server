package ru.netology;

import ru.netology.handlers.ResponseCodeSender;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  public static void main(String[] args) {

    final var server = new Server();
    // код инициализации сервера (из вашего предыдущего ДЗ)

    // добавление хендлеров (обработчиков)
    server.addHandler("GET", "/messages", new Handler() {
      public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        // TODO: handlers code
        var path = request.getPath() + ".html";
        final var filePath = Path.of(".", "public", path);
        final var length = Files.size(filePath);
        final var mimeType = Files.probeContentType(filePath);
        new ResponseCodeSender().sendCode200(responseStream,filePath, length);
        Files.copy(filePath, responseStream);
        responseStream.flush();
      }
    });
    server.addHandler("POST", "/messages", new Handler() {
      public void handle(Request request, BufferedOutputStream responseStream) {
        // TODO: handlers code
      }
    });

    server.listen(9999);
  }
}


