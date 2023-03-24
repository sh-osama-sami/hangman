package server;

import client.Client;
import server.model.Game;
import server.model.Model;
import server.model.Team;
import server.model.ThreadIDUserName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    static PrintWriter serveroutput =null;
    static BufferedReader serverInput = null;

    public static void main(String[] args) {

        int portNumber = 6666;
        int port = 8888;
        try {

            ServerSocket server = new ServerSocket(portNumber);
            Socket clientSocket = null;
            if(args.length>0)
                port = Integer.parseInt(args[0]);

            Socket server2Socket = new Socket("localhost", port);
            //Listening for incoming connections
            while(true) {
                serveroutput = new PrintWriter(server2Socket.getOutputStream(), true);
                serverInput = new BufferedReader(new InputStreamReader(server2Socket.getInputStream()));
                System.out.println("Waiting for a connection...");
                System.out.println("Server is listening on port " + portNumber);


                //Accepting connection
                clientSocket = server.accept();
                System.out.println("A connection has been made!");

                //Creating a thread for the new client
                ClientThread newClient = new ClientThread(clientSocket,serverInput);


                newClient.start();
                System.out.println("Client thread started" + newClient.getId());

            }

        } catch (IOException e) {
            System.out.println("Error on server: " + e);
        }
    }

    public static void sendRandomWordRequest( ) {
        serveroutput.println("/RANDOM_WORD" );
    }

}
