package org.chat.javachatapplication.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    public static final int PORT = 1234;
    static final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());
    static final File logFile = new File("logs/chat_log.txt");

    public static void main(String[] args) {
        if (!logFile.getParentFile().exists()) logFile.getParentFile().mkdirs();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Server started on port " + PORT);
            System.out.println("Waiting for clients...");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void log(String msg) {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = "[" + ts + "] " + msg;
        System.out.println(line);
        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write(line + System.lineSeparator());
        } catch (IOException ignored) {}
    }

    // broadcast helper
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler ch : clients.values()) {
                ch.send(message);
            }
        }
        log("Broadcast: " + message);
    }
}
