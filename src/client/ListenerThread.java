package client;

import client.ui.UIController;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

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
                }
//                else if(input.startsWith("/LIST")) {
//                    String usernames = input.substring(6);
//                    Client.onlineLista = Client.parseList(usernames);
////                    UIController.updateTable();
//                }
                else if (input.startsWith("/CREATE_TEAM")) {
                    response = input.split(":")[1];
                    System.out.println("Server: team creation " + response);
                    UIController.handleCreateTeamResponse(response);
                } else if (input.startsWith("/JOIN_TEAM")) {
                    response = input.split(":")[1];
                    UIController.handleJoinTeamResponse(response);
                }
                else if(input.startsWith("/START_SINGLE_PLAYER_GAME:")){
                    response = input.split(":")[1];
                    UIController.handleStartSinglePlayerGameResponse(response);
                } else if (input.startsWith("GUESS")) {
                    response = input.split(" ")[1];
                    String maskedWord = input.split(" ")[2];
                    UIController.handleGuessResponse(response, maskedWord);

                }
                else if (input.startsWith("/CHECK_FOR_TEAM:"))
                {
                    System.out.println("input " + input);
                    response = input.split(":")[1];
                    ArrayList<String> teamNames = new ArrayList<>();

                    for (int i = 2 ; i < input.split(":").length ; i++)
                    {
                        teamNames.add(input.split(":")[i]);
                    }

                } else if (input.startsWith("/CHECK_FOR_TEAM_STATE")) {
                     response = input.split(":")[1];
                    UIController.handleTeamStateResponse(response);

                } else if (input.startsWith("/NOTIFY_ALL")) {
                    response = input.split(":")[1];
                    UIController.handleGameStartedResponse(response);
                    
                }
                else if (input.startsWith(" "))
                {

                    UIController.handleNotifyPlayer(input);
                }
                else if(input.startsWith("/YOUR_TURN"))
                {
                    response = input.split(":")[1];
                    UIController.handleYourTurnResponse(response);
                }
//                else if (input.startsWith("/MASKED_WORD")) {
//                    response = input.split(":")[1];
//                    UIController.handleMaskedWordResponse(response);
//                }
                else if(input.startsWith("/QUIT_SENT")){
                    String name=input.split(":")[1];
                    UIController.receiveQuitTheGameSignal(name);
                }
                else if (input.startsWith("MaxAttempts")){
                    String maxAttempts=input.split(":")[1];
                    char maxAttemptsChar=maxAttempts.charAt(0);
                    String minTeamSize=input.split(":")[2];
                    char minTeamSizeChar=minTeamSize.charAt(0);
                    String maxTeamSize=input.split(":")[3];
                    char maxTeamSizeChar=maxTeamSize.charAt(0);
                    String minGameRoomSize=input.split(":")[4];
                    char minGameRoomSizeChar=minGameRoomSize.charAt(0);
                    String maxGameRoomSize=input.split(":")[5];
                    char maxGameRoomSizeChar=maxGameRoomSize.charAt(0);
                    UIController.handleMaxAttemptsResponse(maxAttemptsChar,minTeamSizeChar,maxTeamSizeChar,minGameRoomSizeChar,maxGameRoomSizeChar);
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
