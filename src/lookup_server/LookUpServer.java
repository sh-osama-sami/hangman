package lookup_server;

import server.model.Model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class LookUpServer {
    public static ArrayList<String> phrases = new ArrayList<String>();

    public static void main(String[] args) {

        int portNumber = 8888;

        try {

            ServerSocket server = new ServerSocket(portNumber);
            Socket clientSocket = null;

            //Listening for incoming connections
            while(true) {
                System.out.println("Waiting for a connection...");
                System.out.println("Server is listening on port " + portNumber);

                phrases = Model.loadLookUpFile();
                System.out.println("Lookup file loaded" );
                //Accepting connection
                clientSocket = server.accept();
                System.out.println("A connection has been made!");

                //Creating a thread for the new client
                LookupClientThread newClient = new LookupClientThread(clientSocket);


                newClient.start();
                System.out.println("Client thread started" + newClient.getId());
            }

        } catch (IOException e) {
            System.out.println("Error on server: " + e);
        }
    }
}
