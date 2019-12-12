package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private Integer port;
    private ServerSocket serverSocket;

    private boolean lis = true;


    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ConcurrentHashMap<SlaveWorker, String> userList = new ConcurrentHashMap<>();

    /**
     * @param port Port über den der Server läuft (default 5050)
     */
    public Server( Integer port ) {
        if (port != null) this.port = port;

        this.listen();
    }


    public void listen() {

        try {
            serverSocket = new ServerSocket(port);
            int slaveCounter = 0;

            while ( this.lis ) {
                Socket clientSocket = serverSocket.accept();
                SlaveWorker client = new SlaveWorker(this, clientSocket);
                executorService.submit(client);
                userList.put(client, "Client" + slaveCounter);
                System.out.println("New Slave added: Client"+slaveCounter);
                slaveCounter++;
            }
        }
        catch ( Exception ex ) {

        }

    }

}
