package org.chat.javachatapplication.server;

import org.chat.javachatapplication.util.EncryptionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private boolean useEncryption = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // prompt for name
            out.println("ENTER_NAME");
            username = in.readLine();
            if (username == null || username.isBlank()) {
                close();
                return;
            }

            // ensure unique username
            synchronized (ChatServer.clients) {
                String base = username;
                int k = 1;
                while (ChatServer.clients.containsKey(username)) {
                    username = base + k++;
                }
                ChatServer.clients.put(username, this);
            }

            ChatServer.log("Client connected: " + socket + " as " + username);
            ChatServer.broadcast("[Server]: " + username + " joined the chat");

            // main read loop
            String line;
            while ((line = in.readLine()) != null) {
                if (useEncryption) {
                    line = EncryptionUtil.encrypt(line);
                }
                // handle commands
                if (line.equalsIgnoreCase("/quit") || line.equalsIgnoreCase("bye")) {
                    out.println("[Server]: Goodbye " + username);
                    break;
                } else if (line.startsWith("/msg ")) {
                    // /msg target message...
                    String[] parts = line.split(" ", 3);
                    if (parts.length >= 3) {
                        sendPrivate(parts[1], parts[2]);
                    } else {
                        out.println("[Server]: Usage: /msg <user> <text>");
                    }
                } else if (line.equalsIgnoreCase("/encrypt on")) {
                    useEncryption = true;
                    out.println("[Server]: Encryption enabled (Base64).");
                } else if (line.equalsIgnoreCase("/encrypt off")) {
                    useEncryption = false;
                    out.println("[Server]: Encryption disabled.");
                } else {
                    ChatServer.broadcast(username + ": " + line);
                }
            }
        } catch (IOException e) {
            ChatServer.log("Connection error with " + username + ": " + e.getMessage());
        } finally {
            close();
        }
    }

    public void send(String message) {
        // if this client wants encryption, send encoded; otherwise raw
        String toSend = message;
        if (useEncryption) toSend = EncryptionUtil.encrypt(message);
        out.println(toSend);
    }

    private void sendPrivate(String target, String msg) {
        ClientHandler ch;
        synchronized (ChatServer.clients) {
            ch = ChatServer.clients.get(target);
        }
        if (ch != null) {
            ch.send("(Private) " + username + ": " + msg);
            out.println("(Private to " + target + ") " + username + ": " + msg);
            ChatServer.log(username + " -> " + target + " (private): " + msg);
        } else {
            out.println("[Server]: User '" + target + "' not found.");
        }
    }

    private void close() {
        try {
            if (username != null) {
                synchronized (ChatServer.clients) {
                    ChatServer.clients.remove(username);
                }
                ChatServer.broadcast("[Server]: " + username + " left the chat");
            }
            if (socket != null && !socket.isClosed()) socket.close();
            ChatServer.log("Closed connection for " + username);
        } catch (IOException ignored) {}
    }
}
