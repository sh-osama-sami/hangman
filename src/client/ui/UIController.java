package client.ui;

import client.Client;

import java.util.Scanner;

public class UIController extends Thread {
    static String usernameToValidate = "";
    static int maxTeamSize ;

    static int minTeamSize ;

    static int minGameSize ;

    static int maxGameSize ;
    public static boolean giving = true;


     static String teamNameToValidate = "";


     private static void getConfigData() {
         Client.getConfigData();

     }



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

    public static void handleGuessResponse(String response, String maskedWord) {
        if (response.contains("WON")) {
                showGameOptions();
        }
        else if (response.contains("LOST")) {
                showGameOptions();
        }
        else{
            guessCharacterMenu();
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


    public static void handleNotifyPlayer(String response) {
        if (response.contains("won")){
            showGameOptions();
        }
        if (response.contains("lost")) {
            showGameOptions();
        }
        if (response.contains("draw")) {
            showGameOptions();
        }
        if (response.contains("guess")) {
            guessCharacterGameRoomMenu();
        }
        if (response.contains("game mode")) {
            showGameOptions();
        }

    }

    private static void guessCharacterGameRoomMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Guess a character: ");
            String guess = sc.nextLine();
            if (guess.length() == 1 && !guess.equals("-")) {
                char guessedChar = guess.charAt(0);
                Client.sendGuessToServerGameRoom(String.valueOf(guessedChar), usernameToValidate);
                break;
            }else if (guess.equals("-")) {
                Client.sendQuitTheGameSignal();
            }
            else {
                System.out.println("Invalid guess. Please enter a single character.");
            }
        }
    }

    public static void  handleMaxAttemptsResponse(char maxAttempts, char minTeamSize2, char maxTeamSize2, char minGameRoomSize, char maxGameRoomSize) {
         maxGameSize = Integer.parseInt(String.valueOf(maxGameRoomSize));
         minGameSize = Integer.parseInt(String.valueOf(minGameRoomSize));
         maxTeamSize = Integer.parseInt(String.valueOf(maxTeamSize2));
         minTeamSize = Integer.parseInt(String.valueOf(minTeamSize2));
    }

    public void run() {
         Scanner sc = new Scanner(System.in);
        getConfigData();
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
            System.out.println("3. Game Room");
            System.out.println("4. View score history");
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
            else if (choice.equals("3")) {
                showGameRoomOptions(sc);
                break;
            }
            else if (choice.equals("4")) {
                showScoreHistory();
                break;
            }
            else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    private static void showGameRoomOptions(Scanner sc) {
        while (true) {
            System.out.println("Choose game room size:");
            System.out.println("Between " + minGameSize + " - " + maxGameSize + "and wait for other players to join the game room.");
            String choice = sc.nextLine();
            if (choice.equals("-")) {
                Client.sendExitSignal();
            }
            else if (Integer.parseInt(choice) >= minGameSize && Integer.parseInt(choice) <= maxGameSize) {
                Client.sendGameRoomSizeToServer(choice);
                break;
            }
            else {
                System.out.println("Invalid choice. Please enter a number between " + minGameSize + " - " + maxGameSize );
            }
        }
    }

    private static void showScoreHistory() {
        Client.sendScoreHistoryRequest(usernameToValidate);
//        showGameOptions();
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
        System.out.println("Enter team size between: " + minTeamSize + " - " + maxTeamSize);
        String teamSize = sc.nextLine();
        if (Integer.parseInt(teamSize) >= minTeamSize && Integer.parseInt(teamSize) <= maxTeamSize) {
            Client.sendCreateTeamRequest(teamName, teamSize);
        }
        else {
            System.out.println("Invalid choice. Please enter a number between " + minTeamSize + " - " + maxTeamSize );

        }
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