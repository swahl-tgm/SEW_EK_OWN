package server;

import msg.MessageProtocol;

import javax.naming.SizeLimitExceededException;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SlaveWorker implements Runnable {

    private Socket socket;
    private SlaveWorker enm;
    private Server callback;

    private PrintWriter out;
    private BufferedReader in;

    private boolean listening = false;

    private String name;

    public String getName() {
        return name;
    }


    public SlaveWorker(Server callback, Socket socket, String name) {
        this.callback = callback;
        this.socket = socket;
        this.enm = null;
        this.name = name;
    }

    public void setEnm( SlaveWorker enm) {
        this.enm = enm;
        if ( enm != null) {
            out.println(MessageProtocol.ENMSET);
        }
        else {
            out.println(MessageProtocol.ENMUNSET);
        }
    }

    public SlaveWorker getEnm() {
        return this.enm;
    }

    private void shutdown() {
        try {
            this.listening = false;
            this.in.close();
            this.out.close();
            this.callback.remove(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendReady() {
        this.out.println(MessageProtocol.READY);
    }

    private void sendShip(String ship ) {
        this.out.println(ship);
    }


    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);

            listening = true;
            while ( listening ) {
                System.out.println("Slave am hören");
                String msg;
                while ((msg = in.readLine()) != null ) {
                    int ind = msg.indexOf(" ");
                    if ( ind == -1 ) {
                        ind = msg.length();
                    }
                    String command = msg.substring(0, ind);
                    System.out.println("Command: " + command);
                    switch (command) {
                        case MessageProtocol.READY:
                            this.enm.sendReady();
                            break;
                        case MessageProtocol.EXIT:
                            this.shutdown();
                            break;
                        case MessageProtocol.SHIP:
                            this.enm.sendShip(msg);
                            break;
                    }
                    System.out.println(msg);
                }
            }
        } catch (IOException e) {
            // shutdown?
            this.shutdown();
        }
    }
}
