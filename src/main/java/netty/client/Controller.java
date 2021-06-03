package netty.client;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import netty.Message;

public class Controller implements Initializable {

    public TextField input;
    public ListView<String> listView;
    private Network network;
    private String login = "DefaultUser";

    public void send(ActionEvent actionEvent) {
        network.sendMessage(
                Message.builder()
                        .author(login)
                        .createdAt(LocalDateTime.now())
                        .content(input.getText())
                        .build()
        );
        input.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(msg -> {
            String result = msg.getDateFormatted() + " " + msg.getAuthor() + ": " + msg.getContent();
            Platform.runLater(() -> listView.getItems().add(result));
        });
    }
}
