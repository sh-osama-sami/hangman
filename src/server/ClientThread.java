package server;

import server.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

import static server.Server.onlineUsers;
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

    //Client username
    String username = "";
    ThreadIDUserName threadIDUserName ;
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
                        broadcastActiveGames(createActiveList());
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
                    broadcastActiveGames(createActiveList());
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
                        threadIDUserName = new ThreadIDUserName(threadId,username);
                    }
                }
                if (input.startsWith("/CREATE_TEAM")) {
                    String teamName = input.split(":")[1];
                    String username = input.split(":")[2];
                    String response = createTeam(teamName,username);
                    clientOutput.println("/CREATE_TEAM:" + response);
                    if (response.equals("OK")) {
                        System.out.println(teamName + " has been created.");
                    }
                }
                if (input.startsWith("/JOIN_TEAM")) {
                    String teamName = input.split(":")[1];
                    String userName = input.split(":")[2];
                    String response = joinTeam(teamName,userName);
                    clientOutput.println("/JOIN_TEAM:" + response);
                    if (response.equals("OK")) {
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
                if (input.startsWith("/GUESS")){
                    String guess = input.split(":")[1];
                    String response = guess(guess);
                    clientOutput.println("/GUESS:" + response);
                    if (response.equals("CORRECT")) {
                        System.out.println(guess + " has been guessed.");
                    } else if ( response.equals("WRONG")) {
                        System.out.println(guess + " has been guessed.");
                    }
                }
                if (input.startsWith("/CHECK_FOR_TEAM")){
                    String team = input.split(":")[1];
                    System.out.println("from ClientThread: " + team + " checking.");
                    String response = checkForTeamReady(team);
                    System.out.println("from ClientThread response : " + team + " " + response);
                    clientOutput.println("/CHECK_FOR_TEAM:" + response);
                    if (response.equals("OK")) {
                        System.out.println( "A team is ready.");
                    }
                }


//                //This user is inviting someone to play
//                else if (input.startsWith("/INVITE")) {
//                    String name = input.split(":")[1];
//                    forwardInviteTo(name);
//                }
//
//                //This user is receiving an invite to play
//                else if (input.startsWith("/INVITEDBY")) {
//                    String name = input.split(":")[1];
//                    //forward to client
//                    this.clientOutput.println("/INVITEDBY" + name);
//                }
//
//                //This user is responding to an invite to play
//                else if (input.startsWith("/RSVPTO")) {
//                    String name = input.split(":")[1];
//                    String response = input.split(":")[2];
//                    forwardResponse(name, response);
//                } else if (input.startsWith("/RST_W_L")) {
//                    String name = input.split(":")[1];
//                    forwardSignalResetWinsLosses(name);
//                } else if (input.startsWith("/WORD")) {
//                    String reciever = input.split(":")[2];
//                    String word = input.split(":")[3];
//                    String category = input.split(":")[4];
//                    forwardSignal(reciever, word, category);
//                } else if (input.startsWith("/PIC")) {
//                    String name = input.split(":")[1];
//                    String url = input.split(":")[2];
//                    forwardPictureChangedSignal(name, url);
//                } else if (input.startsWith("/LETTER")) {
//                    String letter = input.split(":")[1];
//                    String name = input.split(":")[2];
//                    forwardLetterGotWrongSignal(letter, name);
//                } else if (input.startsWith("/GUESSED_LETTER")) {
//                    String letter = input.split(":")[1];
//                    String name = input.split(":")[2];
//                    String index = input.split(":")[3];
//                    forwardLetterGotRightSignal(letter, name, index);
//                } else if (input.startsWith("/NUM_GM_RQ")) {
//                    String name = input.split(":")[1];
//                    String num = input.split(":")[2];
//                    forwardGmeRqNum(name, num);
//                }
//
//                //Forwarding quit signal to another player
//                else if (input.startsWith("/QUIT")) {
//                    String name = input.split(":")[1];
//                    forwardQuitSignal(name);
//                    Server.activeGames.remove(name);
//                    Server.activeGames.remove(this.username);
//                    broadcastActiveGames(createActiveList());
//                    //System.out.println("broadcast");
//                } else if (input.startsWith("/STATUS_WND")) {
//                    String name = input.split(":")[1];
//                    String gameRqNum = input.split(":")[2];
//                    String result = input.split(":")[3];
//                    forwardGameStatusWindow(name, gameRqNum, result);
//                }
//
//                //Forward chat message to user
//                else if (input.startsWith("/CHATSEND")) {
//                    String name = input.split(":")[1];
//                    String message = input.split(":")[2];
//                    forwardMessage(name, message);
//                } else if (input.startsWith("/GAME_OVER")) {
//                    String name = input.split(":")[1];
//                    String msg = input.split(":")[2];
//
//                    forwardGameOverSignal(name, msg);
//                } else if (input.startsWith("/CHNG_RSLT")) {
//                    String name = input.split(":")[1];
//                    String r1 = input.split(":")[2];
//                    String r2 = input.split(":")[3];
//                    forwardResultChangedSignal(name, r1, r2);
//                } else
//                    continue;
            }

        } catch (IOException e) {
            Server.onlineUsers.remove(this);
            broadcastOnlineList(createOnlineList());
            System.out.println(username + " disconnected.");
            return;
        }

    }

    private String checkForTeamReady(String team) {
        for (Team t : teams) {
            if (t.getName().equals(team)) {
                System.out.println("team found from Client thread: " + team);
                int size = t.getPlayers().size();
                int counter = 0;
                for (User u : t.getPlayers()) {
                    for (int i = 0 ; i < Server.threadIDUserNameList.size() ; i++) {
                        if (Server.threadIDUserNameList.get(i).getUserName().equals(u.getUsername())){
                            counter++;
                        }
                    }
                }
                System.out.println("counter: " + counter + " size: " + size
                );
                System.out.println("team players: " + t.playersToString());
                if (counter == size) {
                    return "OK" + ":" + t.playersToString();
                }
            }
        }
        return "NOT_OK" + ":" + "NOT_OK";
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

    private String guess(String guess) {
        char c = guess.charAt(0);
        return game.guessCharacter(c) + ":" + game.getMaskedPhrase() + ":" + game.getRemainingAttempts();

    }

    private String startSinglePlayerGame() {
        ArrayList<String> words = Model.loadLookUpFile();

        game = new SinglePlayerGame(words.get(0),3);
        Server.activeGames.push(game.id);
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
    private String createActiveList() {
        String usernames="/ACTIVEGAMES:";
        if(Server.activeGames.isEmpty()) {
            usernames+="/EMPTY";
            return usernames;
        }
        for(Integer s : Server.activeGames) {
            usernames+=s+";";
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