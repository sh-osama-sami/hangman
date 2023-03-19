package client.ui;

import client.Client;

import java.util.Scanner;

public class UIController extends Thread{
    static String usernameToValidate = "";
    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("1. Login 2. signup");
        String choice = sc.nextLine();
        if(choice.equals("1")){
            System.out.println("enter username");
            String username = sc.nextLine();
            System.out.println("enter password");
            String password = sc.nextLine();

            Client.sendUsernameToServer(username,password);


        }else if (choice.equals("2")){
            System.out.println("enter your name");
            String name = sc.nextLine();
            System.out.println("enter username");
            String username = sc.nextLine();
            System.out.println("enter password");
            String password = sc.nextLine();
            if(validateUsernameLocally(username)) {
                Client.sendUsernameNameToServer(username,name,password);
            }
        }
    }
    public static boolean validateUsernameLocally(String username){
        usernameToValidate =username;
        boolean valid=false;
        if (username.isEmpty()) {
//            JOptionPane.showMessageDialog(welcomeWindow, "You have to enter a username!", "Try again :(", JOptionPane.ERROR_MESSAGE);

//            welcomeWindow.getTextField().requestFocusInWindow();
        } else if(!username.matches("[A-Za-z0-9]+")) {
//            JOptionPane.showMessageDialog(welcomeWindow, "Incorrect username. Please use only letters a-z and/or numbers 0-9", "Try again :(", JOptionPane.ERROR_MESSAGE);
//            welcomeWindow.getTextField().setText("");
//            welcomeWindow.getTextField().requestFocusInWindow();
            System.out.println("Incorrect username. Please use only letters a-z and/or numbers 0-9\", \"Try again :(");
        } else if(username.length()>10) {
//            JOptionPane.showMessageDialog(welcomeWindow, "Username too long. Please use up to 10 characters.", "Try again :(", JOptionPane.ERROR_MESSAGE);
//            welcomeWindow.getTextField().setText("");
//            welcomeWindow.getTextField().requestFocusInWindow();
            System.out.println("Username too long. Please use up to 10 characters.\", \"Try again :(");
        } else {
            valid=true;
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
    public static void validateLoginfromServer(String msg) {
        if (msg.equals("404")) {
//            JOptionPane.showMessageDialog(welcomeWindow, "Username already taken. Please choose a different one.", "Try again :(", JOptionPane.ERROR_MESSAGE);
            System.out.println("Username not found :(");
        }else if (msg.equals("401")) {
            System.out.println("wrong password");
        }
        else {
            System.out.println("logged in successfuly");
            Client.setUsername(usernameToValidate);
//            showConnectingWindow();
        }

    }


}
