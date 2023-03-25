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
                else if(input.startsWith("/START_SINGLE_PLAYER_GAME:")){
                    response = input.split(":")[1];
                    UIController.handleStartSinglePlayerGameResponse(response);
                } else if (input.startsWith("/GUESS:")) {
                    response = input.split(":")[1];
                    String maskedWord = input.split(":")[2];
                    int noOfGuesses = Integer.parseInt(input.split(":")[3]);
                    System.out.println("Server: guess " + response);
                    UIController.handleGuessResponse(response,maskedWord, noOfGuesses);

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
                else if (input.startsWith("/NOTIFY_PLAYER"))
                {
                    response = input.split(":")[1];
                    UIController.handleNotifyPlayer(response);
                }
                else if(input.startsWith("/YOUR_TURN"))
                {
                    response = input.split(":")[1];
                    UIController.handleYourTurnResponse(response);
                } else if (input.startsWith("/MASKED_WORD")) {
                    response = input.split(":")[1];
                    UIController.handleMaskedWordResponse(response);
                }
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
