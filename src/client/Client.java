package client;

import client.ui.UIController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class Client {

    static PrintStream serverOutput = null;
    static BufferedReader serverInput = null;
    public static ListenerThread listener = null;
    static Socket communicationSocket = null;
    public static LinkedList<String> onlineLista = new LinkedList<String>();

    public static ArrayList<UiThreadToUsername> uiThreadToUsernameList = new ArrayList<>();

    private static String playerUsername ="";

    public static void setUsername(String playerUsername) {
        Client.playerUsername = playerUsername;
    }

    public static String getUsername() {
        return playerUsername;
    }
    public static void main(String[] args) {

        try {
            int port = 6666;

            if(args.length>0)
                port = Integer.parseInt(args[0]);

            communicationSocket = new Socket("localhost", port);

            serverOutput = new PrintStream(communicationSocket.getOutputStream());
            serverInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));

            UIController gui = new UIController();

            gui.start();
            System.out.println("gui thread number from client:" + gui.getId());

            //Input from the server
            listener = new ListenerThread(serverInput);
            listener.start();

            if(listener.end==true) {
                communicationSocket.close();
            }

        } catch (Exception e) {
            System.out.println("Can't connect to server, it appears to be down.");
        }

    }
    //Username validation
    public static void sendUsernameNameToServer(String username ,String name ,String pass, long UiID) {

        serverOutput.println("/USERNAME:"+username+":"+name+":"+pass);
    }
    public static void sendUsernameToServer(String username ,String pass , long UiID) {
        System.out.println("/LOGIN:"+username+":"+pass);
        serverOutput.println("/LOGIN:"+username+":"+pass);
    }
    public static synchronized LinkedList<String> parseList(String usernames) {
        LinkedList<String> list = new LinkedList<>();
        String[] userarray = usernames.split(";");
        for (int i = 0; i < userarray.length; i++) {
            if(userarray[i].equals(getUsername())) {
                continue;
            }
            list.add(userarray[i]);
        }
        return list;
    }

    public static void sendCreateTeamRequest(String teamName) {
        serverOutput.println("/CREATE_TEAM:"+teamName + ":" + getUsername());

    }

    public static void sendJoinTeamRequest(String teamName) {
        serverOutput.println("/JOIN_TEAM:"+teamName + ":" + getUsername());
    }
    public static void sendExitSignal() {
        System.exit(0);
        serverOutput.println("/EXIT");
    }
    public static void sendQuitTheGameSignal(String opponent){
        System.exit(0);
        serverOutput.println("/QUIT:"+opponent);
    }

    public static void sendStartSinglePlayerGameRequest() {
        System.out.println("from client");
        serverOutput.println("/START_SINGLE_PLAYER_GAME" );
    }

    public static void sendGuessToServer(String guess) {
        serverOutput.println("/GUESS"+ ":" + guess);
    }

    public static void checkForTeam(String teamName) {
        System.out.println("from client check for team" + teamName);
        serverOutput.println("/CHECK_FOR_TEAM" + ":" + teamName);
    }

  public static void checkTeamState(String teamName) {
      serverOutput.println("/CHECK_FOR_TEAM_STATE:" + teamName);
  }


    public static void sendGuessToServerMultiplayer(String valueOf, String usernameToValidate) {
        serverOutput.println("/GUESS_MULTIPLAYER"+ ":" + valueOf + ":" + usernameToValidate);
    }
}
