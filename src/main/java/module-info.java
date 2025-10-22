module org.chat.javachatapplication {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.chat.javachatapplication.client to javafx.fxml;
    exports org.chat.javachatapplication.client;

    opens org.chat.javachatapplication.server to javafx.fxml;
    exports org.chat.javachatapplication.server;

    opens org.chat.javachatapplication.util to javafx.fxml;
    exports org.chat.javachatapplication.util;
}
