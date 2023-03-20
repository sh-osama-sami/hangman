package client.ui;

import client.Client;

import java.util.Scanner;

public class UIController extends Thread {
    static String usernameToValidate = "";

    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("1. Login 2. Signup");
            String choice = sc.nextLine();
            if (choice.equals("1")) {
                login(sc);
                break;
            } else if (choice.equals("2")) {
                signup(sc);
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
                break;
            }
        }
    }

    private void login(Scanner sc) {
        System.out.println("Enter username:");
        String username = sc.nextLine();
        System.out.println("Enter password:");
        String password = sc.nextLine();
        System.out.println("Username: " + username + " Password: " + password + " from UIController");
        Client.sendUsernameToServer(username, password);
    }

    private void signup(Scanner sc) {
        System.out.println("Enter your name:");
        String name = sc.nextLine();
        System.out.println("Enter username:");
        String username = sc.nextLine();
        System.out.println("Enter password:");
        String password = sc.nextLine();
        if (validateUsernameLocally(username)) {
            Client.sendUsernameNameToServer(username, name, password);
        }
    }

    public static boolean validateUsernameLocally(String username) {
        usernameToValidate = username;
        boolean valid = false;
        if (username.isEmpty()) {
//            JOptionPane.showMessageDialog(welcomeWindow, "You have to enter a username!", "Try again :(", JOptionPane.ERROR_MESSAGE);

//            welcomeWindow.getTextField().requestFocusInWindow();
        } else if (!username.matches("[A-Za-z0-9]+")) {
//            JOptionPane.showMessageDialog(welcomeWindow, "Incorrect username. Please use only letters a-z and/or numbers 0-9", "Try again :(", JOptionPane.ERROR_MESSAGE);
//            welcomeWindow.getTextField().setText("");
//            welcomeWindow.getTextField().requestFocusInWindow();
            System.out.println("Incorrect username. Please use only letters a-z and/or numbers 0-9\", \"Try again :(");
        } else if (username.length() > 10) {
//            JOptionPane.showMessageDialog(welcomeWindow, "Username too long. Please use up to 10 characters.", "Try again :(", JOptionPane.ERROR_MESSAGE);
//            welcomeWindow.getTextField().setText("");
//            welcomeWindow.getTextField().requestFocusInWindow();
            System.out.println("Username too long. Please use up to 10 characters.\", \"Try again :(");
        } else {
            valid = true;
        }
        return valid;
    }

    public static void validateUsernameFromServer(String msg) {
        if (msg.equals("NOT_OK")) {
//            JOptionPane.showMessageDialog(welcomeWindow, "Username already taken. Please choose a different one.", "Try again :(", JOptionPane.ERROR_MESSAGE);
            System.out.println("Username already taken. Please choose a different one Try again :(");
        } else {
            Client.setUsername(usernameToValidate);
//            showConnectingWindow();
        }

    }

    public static void validateLoginFromServer(String msg) {
        if (msg.equals("404")) {
            System.out.println("Username not found :(");
        } else if (msg.equals("401")) {
            System.out.println("Wrong password");
        } else {
            System.out.println("Logged in successfully");
            Client.setUsername(usernameToValidate);
            showGameOptions();
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
                // Single Player logic
                break;
            } else if (choice.equals("2")) {
                showMultiplayerOptions(sc);
                break;
            } else {
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
            } else {
                System.out.println("Invalid choice. Please enter1 or 2.");
            }
        }
    }

    private static void createTeam(Scanner sc) {
        System.out.println("Enter team name:");
        String teamName = sc.nextLine();
        Client.sendCreateTeamRequest(teamName);
    }

    private static void joinTeam(Scanner sc) {
        System.out.println("Enter the team name you want to join:");
        String teamName = sc.nextLine();
        Client.sendJoinTeamRequest(teamName);
    }

    public static void handleCreateTeamResponse(String msg) {
        if (msg.equals("OK")) {
            System.out.println("Team created successfully");
            // Proceed to the game or wait for other players
        } else if (msg.equals("TEAM_NAME_TAKEN")) {
            System.out.println("Team name already taken. Please choose a different one.");
        } else {
            System.out.println("Error creating team. Please try again.");
        }
    }

    public static void handleJoinTeamResponse(String msg) {
        if (msg.equals("JOINED_TEAM")) {
            System.out.println("Joined the team successfully");
            // Proceed to the game or wait for other players
        } else if (msg.equals("TEAM_NOT_FOUND")) {
            System.out.println("Team not found. Please check the team name and try again.");
        } else {
            System.out.println("Error joining the team. Please try again.");
        }
    }
}



