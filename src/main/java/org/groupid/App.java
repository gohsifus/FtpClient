package org.groupid;

import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.groupid.MyFtp.Download;
import org.groupid.MyFtp.FtpClient;
import org.groupid.MyHttp.HttpClient;
import org.groupid.configs.SystemInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App extends Application {

    public Scene scene;
    public FtpClient ftpClient;
    public VBox vBox;
    public static VBox vb;
    public ScrollPane scrollPane;

    public ProgressBar progressBar;


    @Override
    public void start(Stage primaryStage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();


        VBox vBox = new VBox();
        this.vBox = vBox;

        scrollPane = new ScrollPane();
        scrollPane.setContent(vBox);
        scrollPane.setFitToWidth(true);

        TextField url = new TextField("ftp://91.122.30.115/"); //http://www.mayakovsky.ru/performance/mama-kot/
        vBox.getChildren().add(url);
        vBox.setAlignment(Pos.TOP_CENTER);

        Button sendUrl = new Button();
        sendUrl.setText("Send");
        sendUrl.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if(url.getText().startsWith("ftp")){

                    ftpClient = new FtpClient(url.getText()); //ftp://91.122.30.115/
                    ftpClient.login("qwe");

                    updateListOfFile(ftpClient.getDirectories());  //Обновим список файлов

                } else if(url.getText().startsWith("http")){

                    HttpClient httpClient = new HttpClient(url.getText());
                    getHtmForm(httpClient);

                } else {
                    //Ошибка;
                }
            }
        });

        vBox.getChildren().add(sendUrl);
        Scene scene = new Scene(scrollPane, 720, 480);
        primaryStage.setTitle("Main");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void getHtmForm(HttpClient httpClient){

        Stage newWindow = new Stage();

        progressBar = new ProgressBar(0);
        progressBar.progressProperty().bind(httpClient.progressProperty());

        httpClient.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, //
                new EventHandler<WorkerStateEvent>() {

                    @Override
                    public void handle(WorkerStateEvent t) {
                        newWindow.close();

                        vBox.getChildren().clear();

                        TextArea html = new TextArea(httpClient.builder.toString());
                        html.setPrefRowCount(30);
                        //ScrollPane scrollPane = new ScrollPane(html);

                        vBox.getChildren().add(html);

                    }
                });

        Thread thread = new Thread(httpClient);
        thread.start();


        VBox vb = new VBox();
        vb.getChildren().add(progressBar);
        vb.setAlignment(Pos.CENTER);

        Scene secondScene = new Scene(vb, 230, 100);

        newWindow.setTitle("Загрузка");
        newWindow.setScene(secondScene);

        newWindow.show();

    }


    public void updateListOfFile(ArrayList<String> files){

        Button upDir = new Button("^...");
        upDir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                ftpClient.toDirectory(ftpClient.upToDirectory());
                updateListOfFile(ftpClient.getDirectories());
            }
        });

        vBox.getChildren().clear();

        vBox.getChildren().add(upDir);

        for(int i = 0; i < files.size(); i++){

            String nameOfButton = files.get(i);         //Отсекаем ненужное
            Pattern pattern = Pattern.compile("\\D{3} \\d{2}(  | )(\\d{4}|\\d{2}\\D\\d{2})");
            Matcher matcher = pattern.matcher(nameOfButton);
            if(matcher.find()){
                String[] split = (nameOfButton.charAt(0) + nameOfButton.substring(matcher.end())).split("->");
                nameOfButton = split[0];
            }


            Button newFile = new Button(nameOfButton);
            newFile.setPrefWidth(480);
            newFile.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {

                    if(newFile.getText().startsWith("d") || newFile.getText().startsWith("l")){ //Если директория или ссылка

                        ftpClient.toDirectory(newFile.getText().substring(2));
                        ftpClient.upToDirectory();

                        updateListOfFile(ftpClient.getDirectories());

                    } else {

                        Stage stageForgetPath = new Stage();
                        VBox vBox = new VBox();
                        TextField forPath = new TextField("src\\main\\resources\\");
                        Button send1 = new Button("Send");

                        send1.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {

                                Stage newWindow = new Stage();

                                Download down = new Download(ftpClient, newFile.getText().substring(2), forPath.getText());

                                progressBar = new ProgressBar(0);
                                progressBar.progressProperty().bind(down.progressProperty());

                                down.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, //
                                        new EventHandler<WorkerStateEvent>() {

                                            @Override
                                            public void handle(WorkerStateEvent t) {
                                                newWindow.close();
                                                stageForgetPath.close();
                                            }
                                        });

                                Thread thread = new Thread(down);
                                thread.start();

                                VBox vb = new VBox();
                                vb.getChildren().add(progressBar);
                                vb.setAlignment(Pos.CENTER);

                                Scene secondScene = new Scene(vb, 230, 100);

                                newWindow.setTitle("Загрузка");
                                newWindow.setScene(secondScene);

                                newWindow.show();



                            }
                        });


                        vBox.getChildren().add(forPath);
                        vBox.getChildren().add(send1);

                        Scene scene2 = new Scene(vBox);

                        stageForgetPath.setTitle("Path");
                        stageForgetPath.setScene(scene2);
                        stageForgetPath.show();

                    }
                }
            });

            vBox.getChildren().add(newFile);
        }
    }


}