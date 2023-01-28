package ru.netology;

import ru.netology.handlers.ResponseCodeSender;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class Request {
    private final String method;
    private final String path;
    private final String protocolVersion;
    private final List <String> headers;
    private final byte[] body;


    private Request(String method, String path, String protocolVersion, List<String> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.protocolVersion = protocolVersion;
        this.headers = headers;
        this.body = body;
    }


    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public static Request parse(BufferedReader in, BufferedOutputStream out, ResponseCodeSender sender) throws IOException {
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");
        System.out.println(requestLine);
        if (parts.length != 3) {
            sender.sendCode404(out);
            throw new NullPointerException();
        } else {
            return new Request(parts[0], parts[1], parts[2], null, null);
        }
    }
}
