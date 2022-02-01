package com.adaysoft.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
        private static List<String> messagesListSync = Collections.synchronizedList(new ArrayList<String>());
        private Socket s = null;

        public Monitor(Socket s) {
            this.s = s;
        }
        
        @Override
        public void run() {
            System.out.println("Conection receiving from : " + s.getInetAddress());
            try ( ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                  ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

                oos.writeObject(messagesListSync);
                String name = (String) ois.readObject();

                String message = "";
                while (!message.equals("bye")) {
                    message = (String) ois.readObject();
                    boolean startMessage = message.startsWith("message");
                    if (startMessage) {
                        long timeMillis = System.currentTimeMillis();
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        Date date = new Date(timeMillis);

                        String timeFormated = sdf.format(date);

                        message = message.substring(message.indexOf(":")+1);

                        String messageToClient = "< " + name + " >" +  "[ " + timeFormated + " ]" + "< " + message + " >";
                        messagesListSync.add(messageToClient);
                        
                        oos.writeObject(messageToClient);
                        
                    } else {
                        if (message.equals("bye")) {
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
