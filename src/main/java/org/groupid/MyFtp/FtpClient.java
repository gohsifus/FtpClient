package org.groupid.MyFtp;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FtpClient {


    private String url;
    private InetAddress ip;
    public Socket socket;         //Управляющее соединение
    private int port;

    public BufferedReader fromServer;
    private BufferedWriter toServer;

    private String ipForData = "";
    private int portForData;
    public Socket socketForData;  //Соединение для передачи данных

    public BufferedReader fromServerForData;


    public FtpClient(String url){

        this.url = url;
        port = 21;

        try {

            ip = InetAddress.getByName(new URL(url).getHost());
            System.out.println(ip);

            socket = new Socket(ip, port);

            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            System.out.println(readLine());

        } catch (UnknownHostException e) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Ошибка подключения по указанному адресу!");
            err.show();

        } catch (ConnectException el){
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Ошибка подключения по указанному адресу!");
            err.show();
            
        } catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Ошибка подключения по указанному адресу!");
            err.show();
        }
    }

    public void sendLine(String msg){

        try {
            toServer.write(msg + "\n");
            toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine(){

        String res = "";
        try {

            res = fromServer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean login(String pass){  //логинимся

        String login = "anonymous";
        sendLine("USER " + login);

        String recv = readLine();
        System.out.println(recv);

        sendLine("PASS " + pass);
        recv = readLine();
        System.out.println(recv);

        return true;
    }

    public boolean doPassiveMode(){ //переходим в пассивный режим и инициализируем ip и порт для данных

        ipForData = "";
        sendLine("PASV");
        String recv = readLine();
        System.out.println(recv);


        String[] split = new String[0];
        Pattern pattern = Pattern.compile("\\(.+\\)");
        Matcher matcher = pattern.matcher(recv);

        if(matcher.find()){
            split = recv.substring(matcher.start()+1, matcher.end()-1).split(",");
        }

        for(int i = 0; i < 4; i++){
            if(i == 3){ipForData += split[i];} else {ipForData += split[i] + ".";} //Формируем ip для соединения данных

        }
        portForData = Integer.parseInt(split[4]) * 256 + Integer.parseInt(split[5]); //Парсим порт который присылает сервер

        System.out.println(ipForData + " " + portForData);

        try {
            socketForData = new Socket(ipForData, portForData);

            fromServerForData = new BufferedReader(new InputStreamReader(socketForData.getInputStream()));
        } catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Ошибка передачи данных!");
            err.show();
        }
        return true;
    }

    public ArrayList<String> getDirectories(){
        doPassiveMode();

        ArrayList<String> res = new ArrayList<>();

        sendLine("LIST");

        System.out.println(readLine());

        String dir = "";
        while(true){
            try {
                dir = fromServerForData.readLine();
                if(dir == null){
                    System.out.println(readLine());
                    break;
                }
                res.add(dir);
            } catch (IOException e) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Error");
                err.setHeaderText("Ошибка получения директории!");
                err.show();
            }
        }
        return res;
    }

    public void toDirectory(String name){  //Перейти к директории

        sendLine("CWD " + name);
        System.out.println(readLine());

    }


    public String upToDirectory(){  //Возвращает директорию на уровень выше

        sendLine("PWD");
        String dir = readLine();

        Pattern pattern = Pattern.compile("\".+\"");
        Matcher matcher = pattern.matcher(dir);

        if(matcher.find()){
            dir = dir.substring(matcher.start()+1, matcher.end()-1);
        }

        if(!dir.equals("/")){
            for(int i = dir.length()-1; i >= 0; i--){

                if(dir.charAt(i) == '/'){
                    dir = dir.substring(0, i);
                    if(dir.length() == 0){
                        dir = "/";
                    }
                    break;
                }
            }
        } else {
            dir = "/";
        }

        return dir;
    }

    /*public void loadFile(String name) {

        doPassiveMode();


        sendLine("TYPE I");
        String recv = readLine();
        System.out.println(recv);

        File res = new File("src\\main\\resources\\" + name);

        sendLine("RETR " + name);
        recv = readLine();
        System.out.println(recv);

        Pattern pattern = Pattern.compile("(\\d+ bytes)");
        Matcher matcher = pattern.matcher(recv);


        int max = 0;
        String integer = "";
        if(matcher.find()) {
            integer = recv.substring(matcher.start() + 1, matcher.end() - 6).replaceAll("^[0-9]", "");

        } else {
            System.out.println(integer);
        }

        max = Integer.parseInt(integer);

        int count = 0;

        try {

            //fromServerForData.close();
            FileOutputStream fileOutputStream = new FileOutputStream(res);

            while (true) {

                int in = socketForData.getInputStream().read();
                count ++;


                if (in == -1) {
                    System.out.println("bnm");
                    System.out.println(readLine());
                    fileOutputStream.close();
                    break;
                } else {

                    fileOutputStream.write(in);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
