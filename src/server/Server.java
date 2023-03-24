package server;

import client.Client;
import server.model.Game;
import server.model.Model;
import server.model.Team;
import server.model.ThreadIDUserName;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static LinkedList<ClientThread> onlineUsers = new LinkedList<ClientThread>();
    public static ArrayList<ThreadIDUserName> threadIDUserNameList = new ArrayList<>();
    public static ArrayList<Game> activeGames = new ArrayList<Game>();
    public static ArrayList<Team> teams = new ArrayList<Team>();



    public static void main(String[] args) {

        int portNumber = 6666;

        try {

            ServerSocket server = new ServerSocket(portNumber);
            Socket clientSocket = null;

            //Listening for incoming connections
            while(true) {
                System.out.println("Waiting for a connection...");
                System.out.println("Server is listening on port " + portNumber);

                Model model = new Model();
                System.out.println(model.loadLookUpFile());
                System.out.println("Lookup file loaded" );
                //Accepting connection
                clientSocket = server.accept();
                System.out.println("A connection has been made!");

                //Creating a thread for the new client
                ClientThread newClient = new ClientThread(clientSocket);


                newClient.start();
                System.out.println("Client thread started" + newClient.getId());
            }

        } catch (IOException e) {
            System.out.println("Error on server: " + e);
        }
    }
}
