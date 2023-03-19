package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    public static LinkedList<ClientThread> onlineUsers = new LinkedList<ClientThread>();
    public static LinkedList<String> activeGames = new LinkedList<String>();

    public static void main(String[] args) {

        int portNumber = 6666;

        try {

            ServerSocket server = new ServerSocket(portNumber);
            Socket clientSocket = null;

            //Listening for incoming connections
            while(true) {
                System.out.println("Waiting for a connection...");

                //Accepting connection
                clientSocket = server.accept();
                System.out.println("A connection has been made!");

                //Creating a thread for the new client
                ClientThread newClient = new ClientThread(clientSocket);
                newClient.start();
            }

        } catch (IOException e) {
            System.out.println("Error on server: " + e);
        }
    }
}
