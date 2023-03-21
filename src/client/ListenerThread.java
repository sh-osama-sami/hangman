package client;

import client.ui.UIController;

import java.io.BufferedReader;
import java.io.IOException;

public class ListenerThread extends Thread{
    BufferedReader serverInput = null;
    public boolean end = false;

    //Constructor
    public ListenerThread(BufferedReader input) {
        this.serverInput=input;
    }

    @Override
    public void run() {
        String response="";
        while(!end) {

            try {
                String input = serverInput.readLine();
                System.out.println("Server: " + input);

                if(input.startsWith("/USERNAME")) {
                    response = input.split(":")[1];
                    UIController.validateUsernameFromServer(response);
                } else if (input.startsWith("/LOGIN")) {
                    response = input.split(":")[1];
                    UIController.validateLoginFromServer(response);
                } else if(input.startsWith("/LIST")) {
                    String usernames = input.substring(6);
                    Client.onlineLista = Client.parseList(usernames);
//                    UIController.updateTable();
                }
                else if (input.startsWith("/CREATE_TEAM")) {
                    response = input.split(":")[1];
                    System.out.println("Server: team creation " + response);
                    UIController.handleCreateTeamResponse(response);
                } else if (input.startsWith("/JOIN_TEAM")) {
                    response = input.split(":")[1];
                    UIController.handleJoinTeamResponse(response);
                }
//                else if(input.startsWith("/INVITEDBY")) {
//                    String name = input.split(":")[1];
//                    GUIControler.receiveInvite(name);
//                }
//                else if(input.startsWith("/RSVPBY")) {
//                    String name = input.split(":")[1];
//                    response = input.split(":")[2];
//                    GUIControler.receiveResponseToInvite(name, response);
//                }
//                else if(input.startsWith("/WORD_SET")){
//                    String word = input.split(":")[1];
//                    String category = input.split(":")[2];
//                    GUIControler.receiveSignalWordSet(word, category);
//                }
//                else if(input.startsWith("/ACTIVEGAMES")) {
//                    String usernames = input.split(":")[1];
//                    if(usernames.equals("/EMPTY")) {
//                        Client.activeGames.clear();
//                        continue;
//                    }
//                    Client.activeGames = Client.parseList(usernames);
//                }
//                else if(input.startsWith("/PIC_CHANGED")){
//                    String url=input.split(":")[1];
//                    GUIControler.receiveSignalHnagmanPicChanged(url);
//                }
//                else if(input.startsWith("W_L_RCV")){
//                    GUIControler.receiveSignalResetWinsLosses();
//                }
//                else if(input.startsWith("/WRONG_LETTER")) {
//                    String letter=input.split(":")[1];
//                    GUIControler.receiveSignalWrongLetter(letter);
//                }
//                else if(input.startsWith("/RIGHT_LETTER")) {
//                    String letter=input.split(":")[1];
//                    String index=input.split(":")[2];
//                    GUIControler.receiveSignalRightLetter(letter, index);
//                }
//                else if(input.startsWith("/STATUS_WND_RCV")) {
//                    String gameRqNum=input.split(":")[1];
//                    String result=input.split(":")[2];
//                    GUIControler.receiveSignalStatusWindow(gameRqNum, result);
//                }
//                else if(input.startsWith("/RSLT_CHNGD")) {
//                    String r1=input.split(":")[1];
//                    String r2=input.split(":")[2];
//                    GUIControler.receiveSignalResultChanged(r1, r2);
//                }
//                else if(input.startsWith("/GAME_OVER_RCV")) {
//                    String msg=input.split(":")[1];
//                    GUIControler.receiveGameOverSignal(msg);
//                }
//                else if(input.startsWith("/NUM_RCV")) {
//                    String num=input.split(":")[1];
//                    GUIControler.receiveGameRqNum(num);
//                }
//                else if(input.startsWith("/CHATRCV")) {
//                    String name = input.split(":")[1];
//                    String message = input.split(":")[2];
//                    GUIControler.addMessage(name, message);
//                }
                else if(input.startsWith("/QUIT_SENT")){
                    String name=input.split(":")[1];
                    UIController.receiveQuitTheGameSignal(name);
                }
                else
                    continue;

            } catch (IOException e) {
                System.out.println("Server is down.");
                end = true;
                return;
            } catch(NullPointerException n) {
                return;
            }
        }
    }
}
