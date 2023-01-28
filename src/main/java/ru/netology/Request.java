package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.netology.handlers.ResponseCodeSender;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final String protocolVersion;
    private final List<String> headers;
    private final byte[] body;
    private static Map<String, List<String>> queryParams;


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


    public static Request parse(BufferedInputStream in, BufferedOutputStream out, ResponseCodeSender sender) throws IOException {
        Request request;
        // лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            ResponseCodeSender.sendCode400(out);
            return null;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            ResponseCodeSender.sendCode400(out);
            return null;
        }
        final var method = requestLine[0];
        var pathLine = requestLine[1].split("\\?");
        final var path = pathLine[0];
        if (pathLine.length > 1) {
            getParams(pathLine[1]);
        }

        System.out.println(path);
        System.out.println(method);


        if (!path.startsWith("/")) {
            ResponseCodeSender.sendCode400(out);
            return null;
        }

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            ResponseCodeSender.sendCode400(out);
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        // для GET тела нет
        byte[] body = null;
        if (!method.equals("GET")) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                body = bodyBytes;
            }
        }
        return new Request(method, path, requestLine[2], headers, body);
    }

    private static void getParams(String paramLine) {
        Map<String, List<String>> paramMap = new HashMap<>();

        var paramList = URLEncodedUtils.parse(paramLine, StandardCharsets.UTF_8);
        for (NameValuePair param : paramList) {
            if (!paramMap.containsKey(param.getName())) {
                paramMap.put(param.getName(), new ArrayList<>());
            }
            paramMap.get(param.getName()).add(param.getValue());
        }
        queryParams = paramMap;
    }

    private List<String> getParam(String name) {
        return queryParams.get(name);
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static String[] requestLineSearcher(BufferedOutputStream out, byte[] buffer, int read) throws IOException {
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            ResponseCodeSender.sendCode400(out);
            return null;
        }
        return new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
    }
}
