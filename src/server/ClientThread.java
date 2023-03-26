package server;

import server.model.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    User thisuser = null;
    int maxAtemps = Integer.parseInt(Server.configData[0]);

    static HashMap<Socket, String> clients = new HashMap<>();
    static  HashMap<String, String> team1Vsteam2 = new HashMap<>();
    static HashMap<Socket, String> socketUsernameMap = new HashMap<>();



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
                if (input.startsWith("/GET_CONFIG_DATA"))
                {
                    String response = getConfigData();
                    clientOutput.println(response);

                }
                if (input.startsWith("/SCORE_HISTORY:"))
                {
                    String username = input.split(":")[1];
                    System.out.println("Username from if con: "+username);
                    getScoreHistory(username);


                }

                //Exiting app signal received
                if(input.equals("/EXIT")) {
                    if(!Server.onlineUsers.isEmpty()) {
                        Server.onlineUsers.remove(this);
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
                    Server.onlineUsers.remove(this);
                }

                //Username validation
                if (input.startsWith("/USERNAME")) {
                    String username = input.split(":")[1];
                    String name = input.split(":")[2];
                    String pass = input.split(":")[3];
                    String response = signup(name,username, pass);
                    clientOutput.println("/USERNAME:" + response);
                    if (response.equals("OK")) {

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
                    int teamSize = Integer.parseInt(input.split(":")[3]);
                    String response = createTeam(teamName,username,teamSize);
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
                    String username = input.split(":")[1];
                    String response = startSinglePlayerGame(word,username);

                    clientOutput.println(response);

                }
                if (input.startsWith("/GUESS:")){
                    String guess = input.split(":")[1];
                    String response = guess(guess);
                    clientOutput.println("GUESS"+ " " + response);
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
                    System.out.println("response from check for team state: " + response);
                    String senderTeam = clients.get(this.communicationSocket);
                    for (Map.Entry<Socket, String> entry : clients.entrySet()) {
                        if (entry.getValue().equals(senderTeam)) {
                            Socket receiverSocket = entry.getKey();
                            PrintWriter out = new PrintWriter(receiverSocket.getOutputStream(), true);
                            out.println("/CHECK_FOR_TEAM_STATE:"+response);
//                            clientOutput.println("/CHECK_FOR_TEAM_STATE:" + response);
                        }
                    }
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
                    guessMultiplayer(game,guess , team);
//
                }
                if (input.startsWith("/GAME_ROOM_SIZE:"))
                {
                    Server.sendRandomWordRequest();
                    String word =listener.getWordFromServer();
                    
                    int gameRoomSize = Integer.parseInt(input.split(":")[1]);
                    String username = input.split(":")[2];
                    String res = joinGameRoom(gameRoomSize, username,word);
                    if (res.equals("NEW"))
                    {
                        notifyPlayerWithAResult( username ,"game room isn't full, wait for other players to join");
                    }
                }
                if (input.startsWith("/GUESS_GAMEROOM"))
                {
                    String guess = input.split(":")[1];
                    String username = input.split(":")[2];
                    System.out.println("game room from client thread: " + game);
                    GameRoom game = findGameRoomByUsername(username);
                    System.out.println("game room2 from client thread: " + game);
                    guessGameRoom(guess, game);
                }



            }

        } catch (IOException e) {
            Server.onlineUsers.remove(this);

            System.out.println(username + " disconnected.");
            return;
        }

    }

    private GameRoom findGameRoomByUsername(String username) {
        for (GameRoom gameRoom : Server.gameRooms) {
            for (User user : gameRoom.getUsers()) {
                if (user.getUsername().equals(username))
                    return gameRoom;
            }
        }
        return null;
    }

    private void guessGameRoom(String guess, GameRoom game) throws IOException {
        System.out.println("guess from guessGameRoom: " + guess);
        char guessedChar = guess.charAt(0);
        if (game != null) {
            if (game.guessCharacter(guessedChar)) {
                if (game.isGameOver()) {
                    notifyGameRoomMembers(game, "Game over. " );
                    User winner = game.getWinner();
                    if (winner == null) {
                        for (User user : game.getUsers()) {
                            notifyPlayerWithAResult(user.getUsername(), "It's a draw and masked word was: "+ game.getPhrase());              }
                        increaseGameRoomDrawScore(game.getUsers());
                    }
                    else {
                        notifyPlayerWithAResult(winner.getUsername(), "You won the game. The masked word is: " + game.getMaskedPhrase());
                        Score score = winner.getScore();
                        score.GameRoomWins++;
                        winner.setScore(score);
                        Model.updateScore(winner.getScore());
                        notifyGameRoomMembersExceptPlayer(winner, game, "You lost the game and masked word is: " + game.getPhrase()  + " The winner is: " + winner.getUsername());
                        increaseGameRoomLosesScore(game.getUsers(), winner);
                    }
                    Server.gameRooms.remove(game);
                } else {
                    notifyGameRoomMembers(game, "Correct guess by: " + game.getCurrentPlayer() + " The word now is: " + game.getMaskedPhrase());
                    String nextPlayer = nextGameRoomTurn(game);
                    notifyPlayerWithAMessage(nextPlayer, "Your turn to guess"+ "left attempts: " + game.getAttemptsLeft());
                    notifyPlayerWithAResult(nextPlayer, "Your turn to guess");

                }
            }
            else
            {
                if (game.isGameOver()) {
                    notifyGameRoomMembers(game, "Game over. " );
                    User winner = game.getWinner();
                    if (winner == null) {
                        for (User user : game.getUsers()) {
                            notifyPlayerWithAResult(user.getUsername(), "It's a draw and masked word was: "+ game.getPhrase());              }
                        increaseGameRoomDrawScore(game.getUsers());
                    }
                    else {
                        notifyPlayerWithAResult(winner.getUsername(), "You won the game. The masked word is: " + game.getMaskedPhrase());
                        Score score = winner.getScore();
                        score.GameRoomWins++;
                        winner.setScore(score);
                        Model.updateScore(winner.getScore());
                        notifyGameRoomMembersExceptPlayer(winner, game, "You lost the game and masked word is: " + game.getPhrase() + " The winner is: " + winner.getUsername());
                        increaseGameRoomLosesScore(game.getUsers(), winner);
                    }
                    Server.gameRooms.remove(game);
            }
            else
            {

                notifyGameRoomMembers(game, "Wrong guess by: " + game.getCurrentPlayer() + " \nThe word now is: " + game.getMaskedPhrase());
                String nextPlayer = nextGameRoomTurn(game);
                notifyPlayerWithAMessage(nextPlayer, "Your turn to guess"+ "left attempts: " + game.getAttemptsLeft());
                notifyPlayerWithAResult(nextPlayer, "Your turn to guess");

            }
        }
    }
    }

    private void increaseGameRoomDrawScore(List<User> users) {
        for (User user : users) {
            Score score = user.getScore();
            score.GameRoomDraws++;
            user.setScore(score);
            try {
                Model.updateScore(user.getScore());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void increaseGameRoomLosesScore(List<User> users, User winner) throws IOException {
        for (User user : users) {
            if (!user.getUsername().equals(winner.getUsername())) {
                Score score = user.getScore();
                score.GameRoomLosses++;
                user.setScore(score);
                Model.updateScore(user.getScore());
            }
        }
    }

    private void notifyGameRoomMembers(GameRoom game, String s) {
        for (User user : game.getPlayers()) {
            notifyPlayerWithAMessage(user.getUsername(), s);
        }
    }
    private void notifyGameRoomMembersExceptPlayer(User player, GameRoom game, String s) {
        for (User user : game.getPlayers()) {
            if (!user.getUsername().equals(player.getUsername())) {
                notifyPlayerWithAResult(user.getUsername(), s);
            }
        }
    }

    private String joinGameRoom(int gameRoomSize , String username , String word) throws IOException {
        if(Server.gameRooms.size() == 0)
        {
            GameRoom gameRoom = new GameRoom(gameRoomSize,maxAtemps);
            gameRoom.setPhrase(word);
//            User u = Model.loadUserFromFile(username);
            User u = null;
            for (User user : Server.users) {
                if (user.getUsername().equals(username))
                    u = user;
            }
            gameRoom.addPlayerToGameRoom(u);
            Server.gameRooms.add(gameRoom);
            System.out.println("player "+u.getName() +" added to game room");
        }
        else
        {
            for (GameRoom game : Server.gameRooms) {
                if (game.getGameRoomSize() == gameRoomSize) {
                    User u = null;
                    for (User user : Server.users) {
                        if (user.getUsername().equals(username))
                            u = user;
                    }
                    game.addPlayerToGameRoom(u);
                    System.out.println("player "+u.getName() +" added to game room");
                    if (game.getGameRoomSize() == game.getPlayers().size()) {
                        notifyGameRoomMembers(game," game room is full and ready, wait for your turn");
                        startGameRoom(game, word);
                    }
                    break;
                }
                else
                {
                    GameRoom gameRoom = new GameRoom(gameRoomSize,maxAtemps);
//                    User u = Model.loadUserFromFile(username);
                    for (User user : game.getPlayers()) {
                        gameRoom.addPlayerToGameRoom(user);
                    }
//                    gameRoom.addPlayerToGameRoom(u);
                    Server.gameRooms.add(gameRoom);
                    notifyPlayerWithAMessage( username ," game room isn't full, wait for other players to join");

                    System.out.println("player  added to game room");
                    return "NEW";

                }
            }
        }
        return "EXIST";

    }

    private void startGameRoom(GameRoom game, String word) {
        game.setPhrase(word);
        for (User user : game.getPlayers()) {
            notifyPlayerWithAMessage(user.getName(),"Game room Started and masked word is: "+game.getMaskedPhrase());
        }
        Server.activeGameRooms.add(game);
            String nextPlayer = getCurrentPlayerGameRoom(game);
            notifyPlayerWithAResult(nextPlayer,"It's your turn to guess" + "left attempts: " + game.getAttemptsLeft());


    }

    private String getCurrentPlayerGameRoom(GameRoom game) {
        if (game != null) {
            return game.getCurrentPlayer();
        }
        return null;
    }


    private void

    getScoreHistory(String username) {
        System.out.println("get score history from client thread " + username);
        for (int i = 0; i < Server.scores.size(); i++) {
            if (Server.scores.get(i).getUsername().equals(username)) {
                notifyPlayerWithAMessage(username,"Single Game Wins: " + Server.scores.get(i).singleGameScoreWins);
                notifyPlayerWithAMessage(username,"Single Game Losses: " + Server.scores.get(i).singleGameScoreLosses);
                notifyPlayerWithAMessage(username,"Multi Game Wins: " + Server.scores.get(i).multiGameScoreWins);
                notifyPlayerWithAMessage(username,"Multi Game Losses: " + Server.scores.get(i).multiGameScoreLosses);
                notifyPlayerWithAMessage(username,"Multi Game Draws: " + Server.scores.get(i).multiGameScoreDraws);
                notifyPlayerWithAMessage(username, "Game Room Wins: " + Server.scores.get(i).GameRoomWins);
                notifyPlayerWithAMessage(username, "Game Room Losses: " + Server.scores.get(i).GameRoomLosses);
                notifyPlayerWithAMessage(username, "Game Room Draws: " + Server.scores.get(i).GameRoomDraws);
            }
        }
        notifyPlayerWithAResult(username,"back to game mode menu");
    }

    private String getConfigData() {
        String[] configData ;
        try {
            configData = Server.configData;
            return  "MaxAttempts:"+configData[0] + " Min Team Size:" + configData[1] + " MAx Team:" + configData[2] + " Min Game room size:" + configData[3] + " Max Game Room size:" + configData[4];
        }
        catch (Exception e) {
            return "FAIL";
        }
    }

    private String joinGame(String team , String word ) {
        System.out.println("teamNamesReady size: " + Server.teamReady.size());
        Team currentTeam = null;
        for(Team t : Server.teamReady)
        {
            if(t.getName().equals(team))
                currentTeam = t;
        }
        for (Team t : Server.teamReady) {
            if (t.getTeamSize() == currentTeam.getTeamSize() && !t.getName().equals(team)){
                team1Vsteam2.put(t.getName(),team);
                System.out.println("in making a game");
                Server.teamReady.remove(t);
                Server.teamReady.remove(currentTeam);
                return startMultiplayerGame(team, t.getName(),word);
            }
            else {
                Server.teamReady.add(currentTeam);
                return "WAIT";
            }
        }
        return "WAIT";
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

            Game newGame = new Game(Server.configData[0], teamList);
            newGame.setPhrase(word);
            Server.activeGames.add(newGame);
            System.out.println("Game started between " + teamName1 + " and " + teamName2 + ".");
            System.out.println("active games size: " + Server.activeGames.size());
            System.out.println("active games: " + Server.activeGames.get(0));
            System.out.println("active games: " + Server.activeGames.get(0));
            notifyGameMembers(teamName1, teamName2, "A game has started between " + teamName1 + " and " + teamName2 + "." +"\n wait for your turn", newGame);
            String playerName = getCurrentPlayer(newGame);
            notifyPlayerTurn(playerName);
            return "OK";
        }
    }
    private void notifyGameMembers(String teamName1, String teamName2 , String message, Game game) {
        for (Team team : game.getTeams()) {
            if (team.getName().equals(teamName1) || team.getName().equals(teamName2)) {
                for (User player : team.getPlayers()) {

                    notifyPlayerWithAResult( player.getUsername(), message);
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
            currentPlayerThread.clientOutput.println( " " + message);
        }

    }
    private void notifyPlayerWithAMessage(String currentPlayerUsername , String message) {
        ClientThread currentPlayerThread = findClientThreadByUsername(currentPlayerUsername);
        if (currentPlayerThread != null) {
            currentPlayerThread.clientOutput.println(message);
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
    private String guessMultiplayer(Game game, String guessedString , Team team) throws IOException {
    System.out.println("guess multiplayer from server game:" + game + " guessedString: " + guessedString);
        char guessedChar = guessedString.charAt(0);
        if (game != null) {
            if (game.guessCharacter(guessedChar)) {
                if (game.isGameOver()) {
                    notifyGameMembers(team.getName(), getOpponentTeam(team, game).getName(), "Game over. " , game);
                    Team WonTeam = game.checkWonTeam();
                    Score score = WonTeam.getPlayers().get(0).getScore();
                    score.multiGameScoreWins++;
                    WonTeam.getPlayers().get(0).setScore(score);
                    Model.updateScore(score);
                    Score score1 = WonTeam.getPlayers().get(1).getScore();
                    score1.multiGameScoreWins++;
                    WonTeam.getPlayers().get(1).setScore(score1);
                    Model.updateScore(score1);
                    Score score2 = getOpponentTeam(team, game).getPlayers().get(0).getScore();
                    score2.multiGameScoreLosses++;
                    getOpponentTeam(team, game).getPlayers().get(0).setScore(score2);
                    Model.updateScore(score2);
                    Score score3 = getOpponentTeam(team, game).getPlayers().get(1).getScore();
                    score3.multiGameScoreLosses++;
                    getOpponentTeam(team, game).getPlayers().get(1).setScore(score3);
                    Model.updateScore(score3);

                    if (WonTeam == null) {
                        for (Team t : game.getTeams()) {
                            for (User player : t.getPlayers()) {
                                Score score4 = player.getScore();
                                score4.multiGameScoreDraws++;
                                player.setScore(score4);
                                Model.updateScore(score4);
                            }
                        }
                        notifyPlayerWithAResult(team.getPlayers().get(0).getUsername(), "It's a draw:" + game.getMaskedPhrase());
                        notifyPlayerWithAResult(team.getPlayers().get(1).getUsername(), "It's a draw:" + game.getMaskedPhrase());
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
                    notifyGameMembers(team.getName(), getOpponentTeam(team, game).getName(), "Correct char by team: " + team.getName() + " \nThe word now is: " + game.getMaskedPhrase() , game);
                    String nextPlayer = nextTurn(game);
                    notifyTeamMembers(team, "Your team score is: " + team.getScore() + "remaining attempts" + team.getMaxAttempts());
                    notifyPlayerTurn(nextPlayer);
                    return "CORRECT:" + game.getMaskedPhrase();

                }
            } else {
                if (game.isGameOver()) {
                    notifyGameMembers(team.getName(), getOpponentTeam(team, game).getName(), "Game over. " , game);
                    Team WonTeam = game.checkWonTeam();
                    Score score = WonTeam.getPlayers().get(0).getScore();
                    score.multiGameScoreWins++;
                    WonTeam.getPlayers().get(0).setScore(score);
                    Model.updateScore(score);
                    Score score1 = WonTeam.getPlayers().get(1).getScore();
                    score1.multiGameScoreWins++;
                    WonTeam.getPlayers().get(1).setScore(score1);
                    Model.updateScore(score1);
                    Score score2 = getOpponentTeam(team, game).getPlayers().get(0).getScore();
                    score2.multiGameScoreLosses++;
                    getOpponentTeam(team, game).getPlayers().get(0).setScore(score2);
                    Model.updateScore(score2);
                    Score score3 = getOpponentTeam(team, game).getPlayers().get(1).getScore();
                    score3.multiGameScoreLosses++;
                    getOpponentTeam(team, game).getPlayers().get(1).setScore(score3);
                    Model.updateScore(score3);

                    if (WonTeam == null) {
                        for (Team t : game.getTeams()) {
                            for (User player : t.getPlayers()) {
                                Score score4 = player.getScore();
                                score4.multiGameScoreDraws++;
                                player.setScore(score4);
                                Model.updateScore(score4);
                            }
                        }
                        notifyPlayerWithAResult(team.getPlayers().get(0).getUsername(), "It's a draw:" + game.getMaskedPhrase());
                        notifyPlayerWithAResult(team.getPlayers().get(1).getUsername(), "It's a draw:" + game.getMaskedPhrase());
                        notifyPlayerWithAResult(getOpponentTeam(team, game).getPlayers().get(0).getUsername(), "It's a draw" + game.getMaskedPhrase());
                        notifyPlayerWithAResult(getOpponentTeam(team, game).getPlayers().get(1).getUsername(), "It's a draw" + game.getMaskedPhrase());
                        return "DRAW:" + game.getMaskedPhrase();
                    }
                }
                    else {
                        notifyGameMembers(team.getName(), getOpponentTeam(team, game).getName(), "Wrong char by team: " + team.getName() + " \nThe word now is: " + game.getMaskedPhrase() , game);
                        String nextPlayer = nextTurn(game);
                        notifyPlayerTurn(nextPlayer);
                        return "WRONG:" + game.getMaskedPhrase();
                    }
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

    private String nextGameRoomTurn(GameRoom game) {
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


    private String createTeam(String teamName , String userName , int teamSize) throws IOException {
        try {
            for (int i = 0; i <teams.size() ; i++) {
                if (teams.get(i).getName().equals(teamName)) {
                    return "TEAM_NAME_TAKEN";
                }
            }
            Team team = new Team(teamName, teamSize);
            System.out.println("team created from Client thread: " + teamName);
            for(User u : Server.users)
            {
                if (u.getUsername().equals(userName))
                {
                    team.addPlayer(u);
                    System.out.println("player"+u.getName()+ "added to team: " + teamName);
                    System.out.println("team players: " + team.playersToString());
                    teams.add(team);
                    return "OK";
                }
            }

        }
        catch (Exception e) {
            System.out.println("team not created from Client thread: " + teamName);
        }
        return "NOT_OK";

    }

    private String joinTeam(String teamName , String userName) throws IOException {

        for (Team team : teams) {
            if (team.getName().equals(teamName)) {
                for (User u : Server.users)
                {
                    if (u.getUsername().equals(userName))
                    {
                        for (int i = 0; i < team.getPlayers().size(); i++) {
                            if (team.getPlayers().get(i).getUsername().equals(userName)) {
                                return "NOT_OK_DUPLICATE";
                            }
                        }
                        u.setMaxAttempts(Integer.parseInt(Server.getConfigData()[0]));
                        team.addPlayer(u);
                        return "OK";
                    }
                }

            }
        }
           return "NOT_OK";
    }

    public static String checkForTeamReady2(String teamName){
        for (Team team : teams) {
            if (team.getName().equals(teamName)) {
               int currentTeamSize = team.getPlayers().size();
                if (currentTeamSize == team.getTeamSize()) {
                    System.out.println("inside check for team ready");
                    Server.teamReady.add(team);
                    return "OK";
                }
                else return "NOT_OK";
            }
        }

        return "NOT_OK";
    }

    private String guess(String guess) throws IOException {
        char c = guess.charAt(0);
        System.out.println("guess left from client thread " + game.getRemainingAttempts());
        if (game.guessCharacter(c)) {
            if (game.isGameOver()) {
                if(game.hasWon())
                {
//                    clientOutput.println("WON: " + "the word is"+game.getMaskedPhrase());
                    Score score = game.user.getScore();
                    score.singleGameScoreWins++;
                    game.user.setScore(score);
                    Model.updateScore(game.user.getScore());
                    return "WON" + " " + "masked word is " + game.getMaskedPhrase();
                }
                else
                {
//                    clientOutput.println("LOST: " +  "the word is"+game.getMaskedPhrase());
                    Score score = game.user.getScore();
                    score.singleGameScoreLosses++;
                    game.user.setScore(score);
                    Model.updateScore(game.user.getScore());
                    return "LOST" + " " +"masked word is " + game.getPhrase();
                }
            } else {

                return "CORRECT" + " " + game.getMaskedPhrase() + " " + game.getRemainingAttempts();
            }

        }
        else {
            if (game.isGameOver()) {
                Score score = game.user.getScore();
                score.singleGameScoreLosses++;
                game.user.setScore(score);
                Model.updateScore(game.user.getScore());
                return "LOST" + " " +"masked word is " + game.getPhrase() + " " + game.getRemainingAttempts();
            }
            return "WRONG" + " " + game.getMaskedPhrase() + " " + game.getRemainingAttempts();
        }
    }

    private String startSinglePlayerGame(String word, String username) throws IOException {
        User user = null;
        for (User u : Server.users)
        {
            if (u.getUsername().equals(username))
            {
                user = u;
            }
        }
        game = new SinglePlayerGame(word,Server.configData[0],user);
        return "Starting single player game... masked word is " + game.getMaskedPhrase();

    }
    public static User getUser(String username) {
        for (User u : Server.users)
        {
            if (u.getUsername().equals(username))
            {
                return u;
            }
        }
        return null;
    }
    private String signup (String name ,String username ,String pass) throws IOException {
        thisuser = getUser(username);
        if (thisuser==null) {
            this.username = username;
            User user = new User(name,username,pass);
            Model.saveUser(user);
            Server.users.add(user);
            Server.onlineUsers.add(this);
            Score score = new Score(username);
            Model.saveScore(score);
            return "OK";
        }
        else return "NOT_OK";
    }
    public static boolean existsInOnlineUsers(String username) {
        boolean flag = false;
        for (ClientThread t : Server.onlineUsers) {
            if (t.username.equals(username)) {
               flag = true;
            }
        }
        return flag;
    }
    private String login (String username ,String pass) throws IOException {
        User u = getUser(username);
        if (u!=null) {
            if(!existsInOnlineUsers(username)){
                if (u.getPassword().equals(pass)) {
                    thisuser = u;
                    this.username = username;
                    Server.onlineUsers.add(this);
                    Server.users.add(u);
                    game.user = u;
                    for (Score score : Server.scores){
                        System.out.println( "score for user "+score.getUsername() + " is " + score.GameRoomLosses);
                        if (score.getUsername().equals(username)){
                            u.setScore(score);
                            System.out.println("score for user inside if "+u.getUsername() + " is " + u.getScore().GameRoomLosses);
                        }
                    }
                    return "OK";
                }
                else return "401";
            }
            else return "LOGGED_IN";


        }
        else return "404";
    }

    private void forwardQuitSignal(String name) throws IOException {
        String senderTeam = clients.get(this.communicationSocket);
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getName().equals(senderTeam)){
                for (int j = 0; j <teams.get(i).getPlayers().size() ; j++) {
                    if (teams.get(i).getPlayers().get(j).getUsername().equals(name)) {
                        teams.get(i).getPlayers().remove(j);
                    }
                }

            }


        }
        String receiverTeam = team1Vsteam2.get(senderTeam);
        if (Server.gameRooms!=null){
        Team team = findTeamByUsername(name);
        Game game = findGameByUsername(name);
        Server.gameRooms.remove(game);
        if (team!=null){
            team.getPlayers().remove(thisuser);
        }
        }

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