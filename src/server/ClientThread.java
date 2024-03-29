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
    BufferedReader serverInput;
    //Constructor
    public ClientThread(Socket clientSocket, BufferedReader serverInput) {
        communicationSocket = clientSocket;
        this.serverInput = serverInput;
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
    static  HashMap<String, String> team1Vsteam2 = new HashMap<>();
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
            lookupListener listener = new lookupListener(serverInput);
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
                    Server.sendRandomWordRequest();

                    String word =listener.getWordFromServer();
                    String response = startSinglePlayerGame(word);
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
                    Server.sendRandomWordRequest();
                    String word =listener.getWordFromServer();
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
                        joinGame(team , word);
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
            try {
                forwardQuitSignal(username);
            } catch (IOException ex) {
                System.out.println("Error forwarding quit signal.");
            }
            System.out.println(username + " disconnected.");
            return;
        }

    }

    private String joinGame(String team , String word ) {
        System.out.println("join game from server joinGame" + team);
        System.out.println("teamNamesReady size: " + teamNamesReady.size());
        if (teamNamesReady.size()>0)
        {
            String team2 = teamNamesReady.get(0);
            team1Vsteam2.put(team2,team);
            teamNamesReady.remove(0);
            return startMultiplayerGame(team, team2,word);
        }
        else {
            teamNamesReady.add(team);
            return "WAIT";
        }
    }



    private String startMultiplayerGame(String teamName1, String teamName2,String word) {
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

            Game newGame = new Game(3, teamList);
            newGame.setPhrase(word);
            Server.activeGames.add(newGame);
            System.out.println("Game started between " + teamName1 + " and " + teamName2 + ".");
            System.out.println("active games size: " + Server.activeGames.size());
            System.out.println("active games: " + Server.activeGames.get(0));
            System.out.println("active games: " + Server.activeGames.get(0));
            notifyGameMembers(teamName1, teamName2, "A game has started between " + teamName1 + " and " + teamName2 + "." +"\n wait for your turn");
            String playerName = getCurrentPlayer(newGame);
            notifyPlayerTurn(playerName);
            return "OK";
        }
    }
    private void notifyGameMembers(String teamName1, String teamName2 , String message) {
        for (Map.Entry<Socket, String> entry : clients.entrySet()) {
            if (entry.getValue().equals(teamName1) || entry.getValue().equals(teamName2)) {
                Socket receiverSocket = entry.getKey();
                try {
                    PrintWriter out = new PrintWriter(receiverSocket.getOutputStream(), true);
                    out.println(message);

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

    private void notifyPlayerWithAResult(String currentPlayerUsername , String message) {
        System.out.println("notify player message: " + currentPlayerUsername);
        ClientThread currentPlayerThread = findClientThreadByUsername(currentPlayerUsername);
        if (currentPlayerThread != null) {
            currentPlayerThread.clientOutput.println("Match result:" + message);
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
                        out.println( maskedWord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
    private Team getOpponentTeam(Team team, Game game) {
        // Find the opponent team from the teams in the game
        for (Team teamInGame : game.getTeams()) {
            if (!teamInGame.getName().equals(team.getName())) {
                return teamInGame;
            }
        }
        return null;
    };
    private String guessMultiplayer(Game game, String guessedString , Team team) {
    System.out.println("guess multiplayer from server game:" + game + " guessedString: " + guessedString);
        char guessedChar = guessedString.charAt(0);
        if (game != null) {
            if (game.guessCharacter(guessedChar)) {
                if (game.isGameOver()) {
                    notifyGameMembers(team.getName(), getOpponentTeam(team, game).getName(), "Game over. " );
                    Team WonTeam = game.checkWonTeam();
                    if (WonTeam == null) {
                        notifyPlayerWithAResult(team.getPlayers().get(0).getUsername(), "DRAW:" + game.getMaskedPhrase());
                        notifyPlayerWithAResult(team.getPlayers().get(1).getUsername(), "DRAW:" + game.getMaskedPhrase());
                        notifyPlayerWithAResult(getOpponentTeam(team, game).getPlayers().get(0).getUsername(), "It's a draw" + game.getMaskedPhrase());
                        notifyPlayerWithAResult(getOpponentTeam(team, game).getPlayers().get(1).getUsername(), "It's a draw" + game.getMaskedPhrase());
                        return "DRAW:" + game.getMaskedPhrase();
                    }

                    notifyPlayerWithAResult(WonTeam.getPlayers().get(0).getUsername(), "You team won the game. With team score of  " + WonTeam.getScore());
                    notifyPlayerWithAResult(WonTeam.getPlayers().get(1).getUsername(), "You team won the game. With team score of "+ WonTeam.getScore() );
                    Team opponentTeam = getOpponentTeam(WonTeam, game);
                    notifyPlayerWithAResult(opponentTeam.getPlayers().get(0).getUsername(), "You team lost the game. With team score of " + opponentTeam.getScore());
                    notifyPlayerWithAResult(opponentTeam.getPlayers().get(1).getUsername(), "You team lost the game. With team score of " + opponentTeam.getScore());
                    return "WON:" + game.getMaskedPhrase();
                } else {
                    notifyGameMembers(team.getName(), getOpponentTeam(team, game).getName(), "Correct guess by team: " + team.getName() + " \nThe word now is: " + game.getMaskedPhrase());
                    String nextPlayer = nextTurn(game);
                    notifyTeamMembers(team, "Your team score is: " + team.getScore() );
                    notifyPlayerTurn(nextPlayer);
                    return "CORRECT:" + game.getMaskedPhrase();

                }
            } else {
                notifyGameMembers(team.getName(), getOpponentTeam(team, game).getName(), "Wrong guess by team: " + team.getName() + " \nThe word now is: " + game.getMaskedPhrase());
                String nextPlayer = nextTurn(game);
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
                for (int i = 0; i < team.getPlayers().size(); i++) {
                    if (team.getPlayers().get(i).getUsername().equals(userName)) {
                        return "NOT_OK_DUPLICATE";
                    }
                }
              team.addPlayer(u);
                return "OK";

//                System.out.println(team.getPlayers().get(1).getName());
//                System.out.println("player"+u.getName()+ "added to team: " + teamName);
//                System.out.println("team players: " + team.playersToString());

//                return "OK";
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

    private String startSinglePlayerGame(String word) {

        game = new SinglePlayerGame(    word,3);

        return "OK";

    }

    private String signup (String name ,String username ,String pass) throws IOException {
        for (int i = 0; i < Server.users.size(); i++) {
            if (Server.users.get(i).getUsername().equals(username)) {
                return "NOT_OK";
            }
        }
        this.username = username;
        User user = new User(name,username,pass);
        Model.saveUser(user);
        Server.onlineUsers.add(this);
        ThreadIDUserName t = new ThreadIDUserName(getId(),username);
        Server.threadIDUserNameList.add(t);
        return "OK";
    }
    private String login (String username ,String pass) throws IOException {
        System.out.println("from login" + username + " " + pass);
        User u = Model.loadUserFromFile(username);
//        System.out.println("from login" + u.getUsername() + " " + u.getPassword());
        for (int i = 0; i < Server.users.size() ; i++) {
            if (Server.users.get(i).getUsername().equals(username)) {
                if (u.getPassword().equals(pass)) {
                    this.username = username;
                    Server.onlineUsers.add(this);
                    ThreadIDUserName t = new ThreadIDUserName(getId(),username);
                    Server.threadIDUserNameList.add(t);
                    return "OK";
                }
                else return "401";
            }
        }
        return "404";
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


    private void forwardQuitSignal(String name) throws IOException {
        Server.activeGames.remove(name);
        Server.activeGames.remove(this.username);
        String senderTeam = clients.get(this.communicationSocket);
//                    String clientname = socketUsernameMap.get(this.communicationSocket);
        //remove player from team

        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getName().equals(senderTeam)){
                for (int j = 0; j <teams.get(i).getPlayers().size() ; j++) {
                    if (teams.get(i).getPlayers().get(i).getUsername().equals(name)) {
                        teams.get(i).getPlayers().remove(j);
                    }
                }

            }


        }
        String receiverTeam = team1Vsteam2.get(senderTeam);
        for (Map.Entry<Socket, String> entry : clients.entrySet()) {
            if (entry.getValue().equals(senderTeam)) {
                Socket receiverSocket = entry.getKey();
                PrintWriter out = new PrintWriter(receiverSocket.getOutputStream(), true);
                out.println("/QUIT_SENT:"+name);
            }
            if (entry.getValue().equals(receiverTeam)) {
                Socket receiverSocket = entry.getKey();
                PrintWriter out = new PrintWriter(receiverSocket.getOutputStream(), true);
                out.println("/QUIT_SENT:"+name);
            }
        }
    }
}