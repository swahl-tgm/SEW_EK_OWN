package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SlaveWorker implements Runnable {

    private Socket socket;
    private Server callback;

    private PrintWriter out;
    private BufferedReader in;

    private boolean listening = false;


    public SlaveWorker(Server callback, Socket socket) {
        this.callback = callback;
        this.socket = socket;

    }

    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream());

            listening = true;
            while ( listening ) {
                System.out.println("Slave am hören");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
