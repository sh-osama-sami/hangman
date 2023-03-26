package server;

import client.Client;

import server.model.*;

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

    public static ArrayList<Game> activeGames = new ArrayList<Game>();
    public static ArrayList<Team> teams = new ArrayList<Team>();
    public static ArrayList<GameRoom> activeGameRooms = new ArrayList<GameRoom>();
    public static ArrayList<User> users = new ArrayList<User>();
    static ArrayList<GameRoom> gameRooms = new ArrayList<>();

    static PrintWriter serveroutput =null;
    static BufferedReader serverInput = null;

    static String[] configData = new String[5];

    public static ArrayList<GameRoom> getGameRooms() {
        return gameRooms;
    }

    public static void setGameRooms(ArrayList<GameRoom> gameRooms) {
        Server.gameRooms = gameRooms;
    }

    public static PrintWriter getServeroutput() {
        return serveroutput;
    }

    public static void setServeroutput(PrintWriter serveroutput) {
        Server.serveroutput = serveroutput;
    }

    public static BufferedReader getServerInput() {
        return serverInput;
    }

    public static void setServerInput(BufferedReader serverInput) {
        Server.serverInput = serverInput;
    }

    public static String[] getConfigData() {
        return configData;
    }

    public static void setConfigData(String[] configData) {
        Server.configData = configData;
    }

    public static ArrayList<Score> getScores() {
        return scores;
    }

    public static void setScores(ArrayList<Score> scores) {
        Server.scores = scores;
    }

    static ArrayList<Score> scores = new ArrayList<Score>();

    public static void main(String[] args) {

        int portNumber = 6666;
        int port = 8888;
        try {
            configData = Model.loadGameConfigFromFile();
            scores = Model.loadAllScoresFromFileToServer();
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
