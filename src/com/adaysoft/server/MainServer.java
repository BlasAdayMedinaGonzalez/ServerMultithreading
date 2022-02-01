package com.adaysoft.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainServer {
    public static void main(String[] args) {
        Socket s = null;
        try (ServerSocket ss = new ServerSocket(8080)){
            System.out.println("Server is now running");
            while (true) {
                s = ss.accept();
                Monitor monitor = new Monitor(s);
                monitor.start();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static class Monitor extends Thread {
        private static ArrayList<String> msgList = new ArrayList<String>();
        private Socket s = null;

        public Monitor(Socket s) {
            this.s = s;
        }
        
        @Override
        public void run() {
            System.out.println("Conection receiving from : " + s.getInetAddress());
            try ( ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                  ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

                oos.writeObject(msgList);
                String name = (String) ois.readObject();

                String msg = "";
                while (!msg.equals("bye")) {
                    msg = (String) ois.readObject();
                    boolean startMessage = msg.startsWith("message");
                    if (startMessage) {
                        long time = System.currentTimeMillis();
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        Date date = new Date(time);

                        String timeFormated = sdf.format(date);

                        msg = msg.substring(msg.indexOf(":")+1);

                        String msgToClient = "< " + name + " >" +  "[ " + timeFormated + " ]" + "< " + msg + " >";
                        msgList.add(msgToClient);
                        
                        
                        oos.writeObject(msgToClient);
                    } else {
                        if (msg.equals("bye")) {
                            oos.writeObject("goodbye");
                        } else {
                            oos.writeObject("Format message incorrect, make sure it start by: 'message:'");
                        }
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println("Connection with : " + s.getInetAddress() + " closed");
        }
    }
}
