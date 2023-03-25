package client.ui;

import client.Client;
import client.UiThreadToUsername;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class UIController extends Thread {
    static String usernameToValidate = "";
    public static boolean giving = true;
    public static ConcurrentHashMap<Long, UIController> threadInstances = new ConcurrentHashMap<>();

     static String teamNameToValidate = "";


    public static void handleStartSinglePlayerGameResponse(String response) {
        if (response.equals("OK")) {
            System.out.println("Starting single player game...");
            guessCharacterMenu();

        } else {
            System.out.println("Failed to start single player game.");
        }
    }

    private static void guessCharacterMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Guess a character: ");
            String guess = sc.nextLine();
            if (guess.length() == 1) {
                char guessedChar = guess.charAt(0);
                Client.sendGuessToServer(String.valueOf(guessedChar));
                break;
            } else {
                System.out.println("Invalid guess. Please enter a single character.");
            }
        }
    }

    private static void guessCharacterMultiplayerMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Guess a character: ");
            String guess = sc.nextLine();
            if (guess.length() == 1 && !guess.equals("-")) {
                char guessedChar = guess.charAt(0);
                Client.sendGuessToServerMultiplayer(String.valueOf(guessedChar), usernameToValidate);
                break;
            }else if (guess.equals("-")) {
                Client.sendQuitTheGameSignal();
            }
            else {
                System.out.println("Invalid guess. Please enter a single character.");
            }
        }
    }

    public static void handleGuessResponse(String response, String maskedWord , int numberOfGuesses) {
        if (response.equals("CORRECT")) {
            System.out.println("Correct guess!");
            System.out.println("Masked word guess response: " + maskedWord);
            if (maskedWord.contains("_")) {
                guessCharacterMenu();
            } else {
                System.out.println("You won!");
                showGameOptions();
            }
        }
         else if (response.equals("WRONG")) {
            System.out.println("Wrong guess!");
            System.out.println("Masked word guess response:" + maskedWord);
            if (numberOfGuesses > 0) {
                guessCharacterMenu();
            } else {
                System.out.println("You lost!");
                showGameOptions();
            }

    }
    }



    public static void handleGameStartedResponse(String response) {
        System.out.println(response);
    }

    public static void handleYourTurnResponse(String response) {
        if (response.equals("OK")) {
            System.out.println("It's your turn!");
            guessCharacterMultiplayerMenu();
        } else {
            System.out.println("Failed to start game.");
        }
    }

    public static void handleMaskedWordResponse(String response) {
        System.out.println("response from masked word: " + response);
        if(response.startsWith("WON"))
        {
            System.out.println("You won!");
            showGameOptions();
        }
        if (response.startsWith("LOST")) {
            System.out.println("You lost!");
            showGameOptions();
        }
        System.out.println("Masked word notification: " + response);
    }

    public static void handleNotifyPlayer(String response) {
        if (response.contains("won")){
            showGameOptions();
        }
        if (response.equals("lost")) {
            showGameOptions();
        }
        if (response.equals("draw")) {
            showGameOptions();
        }

    }

    public void run() {
        threadInstances.put(getId(), this);
        Scanner sc = new Scanner(System.in);

           signupOrLogin();


    }

    public static void  signupOrLogin() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("1. Login 2. Signup ");

            String choice = sc.nextLine();
            if (choice.equals("1")) {
                login(sc);
                break;
            } else if (choice.equals("2")) {
                signup(sc);
                break;
            } else if (choice.equals("-")) {
                Client.sendExitSignal();
            }
            else if (choice.equals("3")) {
                showGameOptions();
                break;
            }
            else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }

        }
    }

    private static void login(Scanner sc) {
        System.out.println("Enter username:");
        String username = sc.nextLine();
        usernameToValidate = username;
        System.out.println("Enter password:");
        String password = sc.nextLine();
        System.out.println("Username: " + username + " Password: " + password + " from UIController");
        Client.sendUsernameToServer(username, password, currentThread().getId());
    }

    private static void signup(Scanner sc) {
        System.out.println("Enter your name:");
        String name = sc.nextLine();
        System.out.println("Enter username:");
        String username = sc.nextLine();
        System.out.println("Enter password:");
        String password = sc.nextLine();
        if (validateUsernameLocally(username)) {
            Client.sendUsernameNameToServer(username, name, password , currentThread().getId());
        }
    }
    private static void showGameOptions() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Choose game mode:");
            System.out.println("1. Single Player");
            System.out.println("2. Multiplayer");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                startSinglePlayerGame();
                break;
            } else if (choice.equals("2")) {
                showMultiplayerOptions(sc);
                break;
            }else if (choice.equals("-")) {
                Client.sendExitSignal();
            }
            else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    private static void showMultiplayerOptions(Scanner sc) {
        while (true) {
            System.out.println("Choose action:");
            System.out.println("1. Create Team");
            System.out.println("2. Join Team");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                createTeam(sc);
                break;
            } else if (choice.equals("2")) {
                joinTeam(sc);
                break;
            }else if (choice.equals("-")) {
                Client.sendExitSignal();
            }
            else {
                System.out.println("Invalid choice. Please enter1 or 2.");
            }
        }
    }

    private static void createTeam(Scanner sc) {
        System.out.println("Enter team name:");
        String teamName = sc.nextLine();
        teamNameToValidate = teamName;
        Client.sendCreateTeamRequest(teamName);
    }

    private static void joinTeam(Scanner sc) {
        System.out.println("Enter the team name you want to join:");
        String teamName = sc.nextLine();
        teamNameToValidate = teamName;
        Client.sendJoinTeamRequest(teamName);

    }

    public static boolean validateUsernameLocally(String username) {
        usernameToValidate = username;
        boolean valid = false;
        if (username.isEmpty()) {
//
        } else if (!username.matches("[A-Za-z0-9]+")) {
//
            System.out.println("Incorrect username. Please use only letters a-z and/or numbers 0-9\", \"Try again :(");
        } else if (username.length() > 10) {
//
            System.out.println("Username too long. Please use up to 10 characters.\", \"Try again :(");
        } else {
            valid = true;
        }
        return valid;
    }

    public static void validateUsernameFromServer(String msg) {
        if (msg.equals("NOT_OK")) {
            System.out.println("Username already taken. Please choose a different one Try again :(");
            signupOrLogin();
        } else {
            Client.setUsername(usernameToValidate);
            Client.uiThreadToUsernameList.add(new UiThreadToUsername(currentThread().getId(), usernameToValidate));
            System.out.println("signed up successfully");
            showGameOptions();

        }

    }

    public static void validateLoginFromServer(String msg) {
        if (msg.equals("404")) {
            System.out.println("404 Username not found :(");
            signupOrLogin();

        } else if (msg.equals("401")) {
            System.out.println("401 Wrong password");
            signupOrLogin();

        } else {
            System.out.println("Logged in successfully");
            Client.setUsername(usernameToValidate);
            Client.uiThreadToUsernameList.add(new UiThreadToUsername(currentThread().getId(), usernameToValidate));
            System.out.println("thread id: " + currentThread().getId());
            showGameOptions();
        }
    }


    public static void handleCreateTeamResponse(String msg) {
        if (msg.equals("OK")) {
            System.out.println("Team created successfully , waiting for other players to join");
            // Proceed to the game or wait for other players

        } else if (msg.equals("TEAM_NAME_TAKEN")) {
            System.out.println("Team name already taken. Please choose a different one.");
            showMultiplayerOptions(new Scanner(System.in));
        } else {
            System.out.println("Error creating team. Please try again.");
            showMultiplayerOptions(new Scanner(System.in));

        }
    }

    private static void play(){

        System.out.println("Team Ready waiting for the other team to be ready");

    }

    public static void handleJoinTeamResponse(String msg) {
        if (msg.equals("OK")) {
            System.out.println("Joined the team successfully");
            Client.checkTeamState(teamNameToValidate);
        } else if (msg.equals("NOT_OK")) {
            System.out.println("Team not found. Please check the team name and try again.");
            showMultiplayerOptions(new Scanner(System.in));
        } else if (msg.equals("NOT_OK_DUPLICATE")) {
            System.out.println("You are already in this team.");
            showMultiplayerOptions(new Scanner(System.in));

        } else {
            System.out.println("Error joining the team. Please try again.");
            showMultiplayerOptions(new Scanner(System.in));
        }
    }

    //Recieve the message when your opponent has left the game
    public static void receiveQuitTheGameSignal(String name) {

        System.out.println("player " + name + " has quit the game");
        showMultiplayerOptions(new Scanner(System.in));

    }

    public static void startSinglePlayerGame() {

        Client.sendStartSinglePlayerGameRequest();

        // Update the UI with the initial game state
    }

    public static void handleTeamStateResponse(String response){
        if (response.equals("OK")){
            System.out.println("Team is ready to play");
            play();
        } else if (response.equals("NOT_OK")){
            System.out.println("Team is not ready to play");
        } else {
            System.out.println("Error checking team state");
        }
    }
    private static void updateSinglePlayerUI() {
    }


}