package server;

import server.model.Model;
import server.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ClientThread extends Thread {
    Socket communicationSocket = null;

    //Constructor
    public ClientThread(Socket clientSocket) {
        communicationSocket = clientSocket;
    }

    //Setting up streams for communication
    BufferedReader clientInput = null;
    PrintStream clientOutput = null;

    //Client username
    String username = "";

    @Override
    public void run() {

        try {

            //Initializing I/O streams
            clientInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
            clientOutput = new PrintStream(communicationSocket.getOutputStream());

            while (true) {
                String input = clientInput.readLine();

//                //Exiting app signal received
//                if(input.equals("/EXIT")) {
//                    if(!Server.onlineUsers.isEmpty()) {
//                        Server.onlineUsers.remove(this);
//                        broadcastOnlineList(createOnlineList());
//                        Server.activeGames.remove(this.username);
//                        broadcastActiveGames(createActiveList());
//                        System.out.println(username+" exited.");
//                    } else
//                        System.out.println("Client disconnected.");
//                    communicationSocket.close();
//                    return;
//                }

                //Username validation
                if (input.startsWith("/USERNAME")) {
                    String username = input.split(":")[1];
                    String name = input.split(":")[2];
                    String pass = input.split(":")[3];

//                    String response = checkUsername(name);
//                    clientOutput.println("/USERNAME:" + response);
//                    if (response.equals("OK")) {
//                        this.username = username;
//                        User user = new User(username,name,pass);
//                        Model.saveUser(user);
//                        Server.onlineUsers.add(this);
////                        broadcastOnlineList(createOnlineList());
////                        broadcastActiveGames(createActiveList());
//                        System.out.println(name + " has joined.");
//                    }
                    String response = signup(name,username, pass);
                    clientOutput.println("/USERNAME:" + response);
                    if (response.equals("OK")) {
                        System.out.println(username + " has joined.");
                    }
                }
                if (input.startsWith("/LOGIN")) {
                    String username = input.split(":")[1];
                    String pass = input.split(":")[2];


//                    String response = checkUsername(name);
//                    clientOutput.println("/USERNAME:" + response);
//                    if (response.equals("OK")) {
//                        this.username = username;
//                        User user = new User(username,name,pass);
//                        Model.saveUser(user);
//                        Server.onlineUsers.add(this);
////                        broadcastOnlineList(createOnlineList());
////                        broadcastActiveGames(createActiveList());
//                        System.out.println(name + " has joined.");
//                    }
                    String response = login(username, pass);
                    clientOutput.println("/LOGIN:" + response);
                    if (response.equals("OK")) {
                        System.out.println(username + " has joined.");
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
    private String signup (String name ,String username ,String pass) throws IOException {

        if (Model.loadUserFromFile(username)==null) {
            this.username = username;
            User user = new User(name,username,pass);
            Model.saveUser(user);
            Server.onlineUsers.add(this);
            return "OK";
        }
        else return "NOT_OK";
    }
    private String login (String username ,String pass) throws IOException {
        User u = Model.loadUserFromFile(username);
        if (Model.loadUserFromFile(username)!=null) {
            if (u.getPassword().equals(pass)) {
                this.username = username;
                Server.onlineUsers.add(this);
                return "OK";
            }
            else return "401";

        }
        else return "404";
    }
//    private String checkUsername(String name) {
//        if (Model.loadUserFromFile(name)==null) {
//            return "OK";
//        }
//        else return "NOT_OK";
//    }
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
        for(String s : Server.activeGames) {
            usernames+=s+";";
        }
        return usernames;
    }
}