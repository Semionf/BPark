package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import entities.Message;
import ocsf.client.ObservableClient;
import controllers.KioskController;

public class BParkKioskApp extends Application {

    private static BParkClient client;
    private static final String serverIP = "localhost";
    private static final int serverPort = 5555;

    @Override
    public void start(Stage primaryStage) throws Exception {
        connectToServer();
        showKioskScreen(primaryStage);
    }

    private void showKioskScreen(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/KioskMain.fxml"));
        Parent root = loader.load();

        // Set main stage in the controller to allow screen switching
        KioskController.setMainStage(stage);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/BParkStyle.css").toExternalForm());
        stage.setTitle("BPark - Kiosk Terminal");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void connectToServer() {
        try {
            client = new BParkClient(serverIP, serverPort);
            client.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class BParkClient extends ObservableClient {
        public BParkClient(String host, int port) {
            super(host, port);
        }

        @Override
        protected void handleMessageFromServer(Object msg) {
            Platform.runLater(() -> {
                try {
                    Object message = msg;
                    if (message instanceof byte[]) {
                        message = ClientMessageHandler.deserialize(msg);
                    }

                    if (message instanceof Message) {
                        ClientMessageHandler.handleMessage((Message) message);
                    } else if (message instanceof String) {
                        ClientMessageHandler.handleStringMessage((String) message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        protected void connectionClosed() {
            System.out.println("Connection closed");
        }

        @Override
        protected void connectionException(Exception exception) {
            System.out.println("Connection error: " + exception.getMessage());
        }
    }

    public static void sendMessage(Message msg) {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(ClientMessageHandler.serialize(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendStringMessage(String msg) {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        if (client != null && client.isConnected()) {
            client.sendToServer("ClientDisconnect");
            client.closeConnection();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}