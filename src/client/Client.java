package client;

public class Client {

    String host;
    int port;

    public Client( String host, Integer port) {
        if ( host == null ) this.host = "localhost";
        if ( port == null) this.port = 5050;
    }


    public static void main( String[] args ) {
        new Client(null, null);
    }
}
