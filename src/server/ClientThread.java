package server;

import server.model.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static server.Server.activeGames;
import static server.Server.teams;


public class ClientThread extends Thread {
    Socket communicationSocket = null;

    //Constructor
    public ClientThread(Socket clientSocket) {
        communicationSocket = clientSocket;
    }

    //Setting up streams for communication
    BufferedReader clientInput = null;
    PrintStream clientOutput = null;
    SinglePlayerGame game = null;

    String teamname = "";

    //Client username
    String username = "";
    ThreadIDUserName threadIDUserName ;
    static HashMap<Socket, String> clients = new HashMap<>();
    static HashMap<Socket, String> socketUsernameMap = new HashMap<>();
    static ConcurrentHashMap<String, Boolean> readyTeams = new ConcurrentHashMap<>();

    static ArrayList<String> teamNamesReady = new ArrayList<>();

    @Override
    public void run() {
        long threadId = getId();

        try {

            //Initializing I/O streams
            clientInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
            clientOutput = new PrintStream(communicationSocket.getOutputStream());

            while (true) {
                String input = clientInput.readLine();

                //Exiting app signal received
                if(input.equals("/EXIT")) {
                    if(!Server.onlineUsers.isEmpty()) {
                        Server.onlineUsers.remove(this);
                        broadcastOnlineList(createOnlineList());
                        Server.activeGames.remove(this.username);
                        System.out.println(username+" exited.");
                    } else
                        System.out.println("Client disconnected.");
                    communicationSocket.close();
                    return;
                }
                //Forwarding quit signal to another player
                else if(input.startsWith("/QUIT")){
                    String name=input.split(":")[1];
                    forwardQuitSignal(name);
                    Server.activeGames.remove(name);
                    Server.activeGames.remove(this.username);

                    //System.out.println("broadcast");
                }

                //Username validation
                if (input.startsWith("/USERNAME")) {
                    String username = input.split(":")[1];
                    String name = input.split(":")[2];
                    String pass = input.split(":")[3];
                    String response = signup(name,username, pass);
                    clientOutput.println("/USERNAME:" + response);
                    if (response.equals("OK")) {
                        threadIDUserName = new ThreadIDUserName(threadId,username);
                        System.out.println(username + " has joined.");
                        socketUsernameMap.put(communicationSocket, username);
                    }
                }
                if (input.startsWith("/LOGIN")) {
                    System.out.println("login from server");
                    String username = input.split(":")[1];
                    String pass = input.split(":")[2];
                    String response = login(username, pass);
                    clientOutput.println("/LOGIN:" + response);
                    if (response.equals("OK")) {
                        System.out.println(username + " has joined.");
                        socketUsernameMap.put(communicationSocket, username);

                    }
                }
                if (input.startsWith("/CREATE_TEAM")) {
                    String teamName = input.split(":")[1];
                    String username = input.split(":")[2];
                    String response = createTeam(teamName,username);
                    clientOutput.println("/CREATE_TEAM:" + response);
                    if (response.equals("OK")) {
                        this.teamname = teamName;
                        clients.put(this.communicationSocket, this.teamname);

                        System.out.println(teamName + " has been created.");
                    }
                }
                if (input.startsWith("/JOIN_TEAM")) {
                    String teamName = input.split(":")[1];
                    String userName = input.split(":")[2];
                    String response = joinTeam(teamName,userName);
                    clientOutput.println("/JOIN_TEAM:" + response);
                    if (response.equals("OK")) {
                        this.teamname = teamName;
                        clients.put(this.communicationSocket, this.teamname);
                        System.out.println(userName + " has joined " + teamName + ".");
                    }
                }
                if (input.startsWith("/START_SINGLE_PLAYER_GAME")) {
                    System.out.println("start single player game from server");
                    String response = startSinglePlayerGame();
                    System.out.println("response from start single player game: " + response);
                    clientOutput.println("/START_SINGLE_PLAYER_GAME:" + response);
                    if (response.equals("OK")) {
                        System.out.println( "A single player game.");
                    }
                }
                if (input.startsWith("/GUESS:")){
                    String guess = input.split(":")[1];
                    String response = guess(guess);
                    clientOutput.println("/GUESS:" + response);
                    if (response.equals("CORRECT")) {
                        System.out.println(guess + " has been guessed.");
                    } else if ( response.equals("WRONG")) {
                        System.out.println(guess + " has been guessed.");
                    }
                }

                if (input.startsWith("/CHECK_FOR_TEAM_STATE")){
                    String team = input.split(":")[1];
                    String response = checkForTeamReady2(team);
                    String senderTeam = clients.get(this.communicationSocket);
                    for (Map.Entry<Socket, String> entry : clients.entrySet()) {
                        if (entry.getValue().equals(senderTeam)) {
                            Socket receiverSocket = entry.getKey();
                            PrintWriter out = new PrintWriter(receiverSocket.getOutputStream(), true);
                            out.println("/CHECK_FOR_TEAM_STATE:"+response);
//                            clientOutput.println("/CHECK_FOR_TEAM_STATE:" + response);
                        }
                    }
                   // clientOutput.println("/CHECK_FOR_TEAM_STATE:" + response);
                    if (response.equals("OK")) {
                        System.out.println( "A team is ready.");
                        joinGame(team);
                    }
                }
                if (input.startsWith("/GUESS_MULTIPLAYER:")){
                    String guess = input.split(":")[1];
                    String player = input.split(":")[2];
                    Game game = findGameByUsername(player);
                    Team team = findTeamByUsername(player);
                    System.out.println("player from the if condition: " + player);
                    String response = guessMultiplayer(game,guess , team);
//                    clientOutput.println("/GUESS_MULTIPLAYER:" + response);
//                    if (response.equals("CORRECT")) {
//                        System.out.println(guess + " has been guessed.");
//                    } else if ( response.equals("WRONG")) {
//                        System.out.println(guess + " has been guessed.");
//                    }

                }



            }

        } catch (IOException e) {
            Server.onlineUsers.remove(this);
            broadcastOnlineList(createOnlineList());
            System.out.println(username + " disconnected.");
            return;
        }

    }

    private String joinGame(String team) {
        System.out.println("join game from server joinGame" + team);
        System.out.println("teamNamesReady size: " + teamNamesReady.size());
        if (teamNamesReady.size()>0)
        {
            String team2 = teamNamesReady.get(0);
            teamNamesReady.remove(0);
            return startMultiplayerGame(team, team2);
        }
        else {
            teamNamesReady.add(team);
            return "WAIT";
        }
    }



    private String startMultiplayerGame(String teamName1, String teamName2) {
        System.out.println("start multiplayer game from server" + teamName1 + " " + teamName2);
        if (teamName1.equals(teamName2)) {
            return "FAIL";
        }
        else {
            ArrayList<Team> teamList = new ArrayList<>();
            for (Team team : teams) {
                if (team.getName().equals(teamName1)) {
                    teamList.add(team); // Add the team to the ArrayList

                }
                if (team.getName().equals(teamName2)) {
                    teamList.add(team); // Add the team to the ArrayList

                }
            }
            ArrayList<String> words = Model.loadLookUpFile();
            Game newGame = new Game(3, teamList);
            newGame.setPhrase(words.get(0));
            Server.activeGames.add(newGame);
            System.out.println("Game started between " + teamName1 + " and " + teamName2 + ".");
            System.out.println("active games size: " + Server.activeGames.size());
            System.out.println("active games: " + Server.activeGames.get(0));
            System.out.println("active games: " + Server.activeGames.get(0));
            notifyGameStart(teamName1, teamName2);
            String playerName = getCurrentPlayer(newGame);
            notifyPlayerTurn(playerName);
            return "OK";
        }
    }
    private void notifyGameStart(String teamName1, String teamName2) {
        for (Map.Entry<Socket, String> entry : clients.entrySet()) {
            if (entry.getValue().equals(teamName1) || entry.getValue().equals(teamName2)) {
                Socket receiverSocket = entry.getKey();
                try {
                    PrintWriter out = new PrintWriter(receiverSocket.getOutputStream(), true);
                    out.println("/GAME_STARTED:"+"OK");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private ClientThread findClientThreadByUsername(String username) {
        System.out.println("find client thread by username: " + username);
        Socket userSocket = null;

        for (Map.Entry<Socket, String> entry : socketUsernameMap.entrySet()) {
            if (entry.getValue().equals(username)) {
                userSocket = entry.getKey();
                break;
            }
        }

        if (userSocket != null) {
            for (ClientThread clientThread : Server.onlineUsers) {
                if (clientThread.communicationSocket.equals(userSocket)) {
                    return clientThread;
                }
            }
        }

        return null;
    }
    private void notifyPlayerTurn(String currentPlayerUsername) {
        System.out.println("notify player turn: " + currentPlayerUsername);
        ClientThread currentPlayerThread = findClientThreadByUsername(currentPlayerUsername);
        if (currentPlayerThread != null) {
            currentPlayerThread.clientOutput.println("/YOUR_TURN:" + "OK");
        }
    }

    private Game findGameByUsername(String username) {
        Team userTeam = null;

        // Find the team the user belongs to
        for (Team team : teams) {
            for (User player : team.getPlayers()) {
                if (player.getUsername().equals(username)) {
                    userTeam = team;
                    break;
                }
            }
            if (userTeam != null) {
                break;
            }
        }

        if (userTeam != null) {
            // Find the game the team is participating in
            for (Game game : activeGames) {
                for (Team team : game.getTeams()) {
                    if (team.getName().equals(userTeam.getName())) {
                        return game;
                    }
                }
            }
        }

        return null;
    }
    private Team findTeamByUsername(String username) {
        Team userTeam = null;

        // Find the team the user belongs to
        for (Team team : teams) {
            for (User player : team.getPlayers()) {
                if (player.getUsername().equals(username)) {
                    userTeam = team;
                    break;
                }
            }
            if (userTeam != null) {
                break;
            }
        }
        return userTeam;
    }

    private void notifyTeamMembers(Team team, String maskedWord) {
        if (team != null) {
            for (User player : team.getPlayers()) {
                String username = player.getUsername();
                Socket playerSocket = null;

                for (Map.Entry<Socket, String> entry : socketUsernameMap.entrySet()) {
                    if (entry.getValue().equals(username)) {
                        playerSocket = entry.getKey();
                        break;
                    }
                }

                if (playerSocket != null) {
                    try {
                        PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
                        out.println("/MASKED_WORD:" + maskedWord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
    private String guessMultiplayer(Game game, String guessedString , Team team) {
    System.out.println("guess multiplayer from server game:" + game + " guessedString: " + guessedString);
        char guessedChar = guessedString.charAt(0);
        String nextPlayer = nextTurn(game);
        if (game != null) {
            if (game.guessCharacter(guessedChar)) {
                if (game.isGameOver()) {
                    notifyPlayerTurn(nextPlayer);
                    return "WON:" + game.getMaskedPhrase();
                } else {
                    notifyTeamMembers(team, game.getMaskedPhrase());
                    notifyPlayerTurn(nextPlayer);
                    return "CORRECT:" + game.getMaskedPhrase();

                }
            } else {
                notifyTeamMembers(team, game.getMaskedPhrase());
                notifyPlayerTurn(nextPlayer);
                return "WRONG:" + game.getMaskedPhrase();
            }
        }
        return "ERROR: Game not found.";

    }

    private String nextTurn(Game game) {
        System.out.println("next turn from server");
        if (game != null) {
            return  game.nextTurn();
        }
        return null;
    }

    private String getCurrentPlayer(Game game) {
        if (game != null) {
            return game.getCurrentPlayer();
        }
        return null;
    }


    private String createTeam(String teamName , String userName) throws IOException {
        try {
            Team team = new Team(teamName);
            System.out.println("team created from Client thread: " + teamName);
            User u = Model.loadUserFromFile(userName);
            team.addPlayer(u);
            System.out.println(team.getPlayers().get(0).getName());
            System.out.println("player"+u.getName()+ "added to team: " + teamName);
            System.out.println("team players: " + team.playersToString());
            teams.add(team);
            return "OK";
        }
        catch (Exception e) {
            System.out.println("team not created from Client thread: " + teamName);
        }
        return "NOT_OK";

    }

    private String joinTeam(String teamName , String userName) throws IOException {
        for (Team team : teams) {
            if (team.getName().equals(teamName)) {
                User u = Model.loadUserFromFile(userName);
                team.addPlayer(u);
                System.out.println(team.getPlayers().get(1).getName());
                System.out.println("player"+u.getName()+ "added to team: " + teamName);
                System.out.println("team players: " + team.playersToString());

                return "OK";
            }
        }
           return "NOT_OK";
    }

    public static String checkForTeamReady2(String teamName){
        for (Team team : teams) {
            if (team.getName().equals(teamName)) {
               int teamSize = team.getPlayers().size();
                if (teamSize >= 2) {
                    return "OK";
                }
            }
        }

        return "NOT_OK";
    }

    private String guess(String guess) {
        char c = guess.charAt(0);
        return game.guessCharacter(c) + ":" + game.getMaskedPhrase() + ":" + game.getRemainingAttempts();

    }

    private String startSinglePlayerGame() {
        ArrayList<String> words = Model.loadLookUpFile();

        game = new SinglePlayerGame(words.get(0),3);

        return "OK";

    };

    private String signup (String name ,String username ,String pass) throws IOException {

        if (Model.loadUserFromFile(username)==null) {
            this.username = username;
            User user = new User(name,username,pass);
            Model.saveUser(user);
            Server.onlineUsers.add(this);
            ThreadIDUserName t = new ThreadIDUserName(getId(),username);
            Server.threadIDUserNameList.add(t);
            return "OK";
        }
        else return "NOT_OK";
    }
    private String login (String username ,String pass) throws IOException {
        System.out.println("from login" + username + " " + pass);
        User u = Model.loadUserFromFile(username);
//        System.out.println("from login" + u.getUsername() + " " + u.getPassword());
        if (Model.loadUserFromFile(username)!=null) {
            if (u.getPassword().equals(pass)) {
                this.username = username;
                Server.onlineUsers.add(this);
                ThreadIDUserName t = new ThreadIDUserName(getId(),username);
                Server.threadIDUserNameList.add(t);
                return "OK";
            }
            else return "401";

        }
        else return "404";
    }

    private void broadcastOnlineList(String list) {
        for (ClientThread t : Server.onlineUsers) {
            t.clientOutput.println(list);
        }
    }
    private void broadcastActiveGames(String activeList) {
        for (ClientThread t : Server.onlineUsers) {
            t.clientOutput.println(activeList);
        }
    }
    private String createOnlineList() {
        String usernames="/LIST:";
        for(ClientThread t : Server.onlineUsers) {
            usernames+=t.username+";";
        }
        return usernames;
    }


    private void forwardQuitSignal(String name) {
        for(ClientThread t : Server.onlineUsers) {
            if(t.username.equals(name)) {
                t.clientOutput.println("/QUIT_SENT:"+this.username);
                return;
            }
        }
    }
}