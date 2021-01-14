package org.groupid.MyHttp;

import javafx.concurrent.Task;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class HttpClient extends Task<StringBuilder> {

    public String url;
    public InetAddress ip;
    public Socket socket;

    public BufferedReader fromServer;
    public BufferedWriter toServer;

    public StringBuilder builder;

    public HttpClient(String url) {
        this.url = url;
    }

    @Override
    protected StringBuilder call() throws Exception {
        builder = getHtml();
        return builder;
    }

    public StringBuilder getHtml() {

        StringBuilder res = new StringBuilder();
        int count = 0, progress = 0, max = 0;

        try {
            URL urlTrue = new URL(url);
            ip = InetAddress.getByName(urlTrue.getHost());

            socket = new Socket(ip, 80);

            PrintWriter wtr = new PrintWriter(socket.getOutputStream());

            wtr.print("GET " + urlTrue.getFile() + " HTTP/1.1\r\n");
            wtr.print("Host: " + urlTrue.getHost() + "\r\n");
            wtr.print("\r\n");
            wtr.flush();

            //System.out.println("Запрос: \nGET " + urlTrue.getFile() + " HTTP/1.1\r\n" + "Host: " + urlTrue.getHost() + "\r\n");

            BufferedReader bufRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String outStr;

            boolean flag = false;
            boolean flagOk = false;

            List<String> header = new ArrayList<>();

            //Читаем ответ
            while(!(outStr = bufRead.readLine()).equals("0")){

                progress += outStr.getBytes().length;
                if(max != 0){
                    updateProgress(progress, max);
                }


                if(outStr.equals("")){

                    flag = true;
                } else if(flag){  //после заголовка
                    count++;
                    if(count == 1 &&  max == 0 && header.contains("Transfer-Encoding: chunked")){
                        max = Integer.parseInt(outStr.toUpperCase(), 16);
                    }
                    res.append(outStr + "\n");
                    //System.out.println(outStr + " " + count + " " + header.size());
                } else{          //заголовок

                    header.add(outStr);
                    System.out.println(outStr);
                    if(outStr.equals("HTTP/1.1 200 OK")){
                        flagOk = true;
                        res.append(outStr);
                    }

                    if(outStr.startsWith("Content-Length:")){
                        String[] split = outStr.split("");
                        max = Integer.parseInt(split[split.length-1]);
                    }
                }
            }
            bufRead.close();
            wtr.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
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

}
