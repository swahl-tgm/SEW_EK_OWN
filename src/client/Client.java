package client;

import client.GUI.ClientController;
import javafx.application.Platform;
import msg.MessageProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Client implements Runnable {

    private String name;
    private String host;
    private int port;
    private Socket socket;
    private ClientController c;

    private BufferedReader in;
    private PrintWriter out;

    private boolean listening;

    public Client( String host, Integer port, ClientController c) throws IOException {
        this.host = Objects.requireNonNullElse(host, "localhost");
        this.port = Objects.requireNonNullElse(port, 5050);
        this.c = c;

        socket = new Socket(this.host, this.port);
        listening = false;
    }

    public void setName(String name ) {
        this.name = name;
    }


    public void shutdown() {
        if ( listening ) {
            try {
                listening = false;
                out.println(MessageProtocol.EXIT);
                in.close();
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send( String msg) {
        this.out.println(msg);
    }


    @Override
    public void run() {
        listening = true;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true);

            String msg;
            while ( (msg = in.readLine()) != null )  {
                int ind = msg.indexOf(" ");
                if ( ind == -1 ) {
                    ind = msg.length();
                }
                String command = msg.substring(0, ind);
                System.out.println("Command client: " + command);
                switch (command) {
                    case MessageProtocol.READY:
                        this.c.setEnmStarted();
                        break;
                    case MessageProtocol.EXIT:
                        this.shutdown();
                        break;
                    case MessageProtocol.HIT:
                        this.c.setTileHit(msg);
                        break;
                    case MessageProtocol.SHIP:
                        this.c.setEnmShip(msg);
                        break;
                    case MessageProtocol.ENMSET:
                        this.c.foundEnm();
                        break;
                    case MessageProtocol.ENMUNSET:
                        this.c.enmDisconnected();
                        break;
                    case MessageProtocol.FIRST:
                        this.c.setAlreadyHit(true);
                        break;
                }
            }
        } catch (IOException e) {
            // Server disconnected
            System.out.println("Server disconnected");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    c.srvDisconnected();
                }
            });
        }
    }
}
