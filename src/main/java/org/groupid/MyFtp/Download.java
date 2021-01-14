package org.groupid.MyFtp;

import javafx.concurrent.Task;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Download extends Task<Void> {

    public FtpClient ftpClient;
    public String name;
    public String path;

    public Download(FtpClient ftpClient, String name, String path) {
        this.ftpClient = ftpClient;
        this.name = name;
        this.path = path;
    }

    @Override
    protected Void call() throws Exception {
        loadFile(name);
        return null;
    }

    public void loadFile(String name) {

        ftpClient.doPassiveMode();

        ftpClient.sendLine("TYPE I");
        String recv = ftpClient.readLine();

        File res = new File("src\\main\\resources\\" + name);

        ftpClient.sendLine("RETR " + name);
        recv = ftpClient.readLine();
        System.out.println(recv);

        Pattern pattern = Pattern.compile("(\\d+ bytes)");
        Matcher matcher = pattern.matcher(recv);


        int max = 0;
        String integer = "";
        if(matcher.find()) {
            integer = recv.substring(matcher.start() , matcher.end() - 6);
            integer = (integer.charAt(0) + integer).replaceAll("^[0-9]", "");

        } else {
            System.out.println(integer);
        }

        max = Integer.parseInt(integer);
        System.out.println(max);

        int count = 0;

        try {

            FileOutputStream fileOutputStream = new FileOutputStream(res);


            while (count <= max) {

                fileOutputStream.write(ftpClient.socketForData.getInputStream().read());
                count ++;

                updateProgress(count, max);

                if (count >= max) {

                    System.out.println(ftpClient.readLine());
                    fileOutputStream.close();
                    ftpClient.fromServerForData.close();
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
