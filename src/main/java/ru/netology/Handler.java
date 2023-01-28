package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

public interface Handler {
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException;
}
