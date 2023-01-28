package ru.netology.handlers;

import ru.netology.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class RequestCollector {
    public Request handle(String requestLine, BufferedOutputStream out, ResponseCodeSender sender) throws IOException {
        final var parts = requestLine.split(" ");
        System.out.println(requestLine);
        if (parts.length != 3) {
            sender.returnCode404(out);
            throw new NullPointerException();
        } else {
            return new Request(parts[0], parts[1], parts[2], null, null);
        }
    }
}
