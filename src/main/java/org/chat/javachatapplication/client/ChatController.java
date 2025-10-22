package org.chat.javachatapplication.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import org.chat.javachatapplication.util.EncryptionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class ChatController {
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean useEncryption = false;

    @FXML
    public void initialize() {
        // ask for username on UI thread
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter Name");
            dialog.setHeaderText("Enter your display name for the chat");
            dialog.setContentText("Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresentOrElse(name -> {
                username = name.trim();
                if (username.isEmpty()) username = "User" + (int)(Math.random()*1000);
                connectToServer();
            }, () -> {
                // user cancelled – close application
                Platform.exit();
            });
        });

        // send on enter key
        messageField.setOnAction(e -> onSend());
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 1234);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // reader thread
                Thread reader = new Thread(this::readLoop);
                reader.setDaemon(true);
                reader.start();

                // send name after server asks
                String first = in.readLine();
                if ("ENTER_NAME".equals(first)) {
                    out.println(username);
                } else {
                    // sometimes server might not send explicit prompt - still send name
                    out.println(username);
                }

                Platform.runLater(() -> chatArea.appendText("[System] Connected as " + username + "\n"));
            } catch (IOException e) {
                Platform.runLater(() -> {
                    chatArea.appendText("[System] Unable to connect to server: " + e.getMessage() + "\n");
                });
            }
        }).start();
    }

    private void readLoop() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                // try decoding if looks base64 (server handles encryption per-client)
                String display = line;
                // we do not auto-decrypt unless user toggles; however if server sent base64 and client uses encryption,
                // the server will mark encryption per-connection. For demo, attempt to decode safely:
                String maybeDecoded = EncryptionUtil.decrypt(display);
                if (!maybeDecoded.equals(display)) {
                    // it was base64 encoded
                    display = maybeDecoded;
                }

                final String msg = display;
                Platform.runLater(() -> chatArea.appendText(msg + "\n"));
            }
        } catch (IOException e) {
            Platform.runLater(() -> chatArea.appendText("[System] Disconnected from server.\n"));
        } finally {
            close();
        }
    }

    @FXML
    private void onSend() {
        String text = messageField.getText();
        if (text == null || text.isBlank()) return;
        if (out == null) {
            chatArea.appendText("[System] Not connected.\n");
            return;
        }
//         if client requested encryption locally, encode before sending
        String toSend = text;
        if (useEncryption) toSend = EncryptionUtil.encrypt(text);




        out.println(toSend);
        messageField.clear();

        if (text.equalsIgnoreCase("/quit") || text.equalsIgnoreCase("bye")) {
            // allow server to respond then close
            new Thread(() -> {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    close();
                    chatArea.appendText("[System] You left the chat.\n");
                });
            }).start();
        } else if (text.equalsIgnoreCase("/encrypt on")) {
            useEncryption = true;
        } else if (text.equalsIgnoreCase("/encrypt off")) {
            useEncryption = false;
        }
    }

    private void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
