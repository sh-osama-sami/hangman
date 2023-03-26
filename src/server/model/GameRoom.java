package server.model;

import server.Server;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private ArrayList<User> users = new ArrayList<>();
    private int gameRoomSize;
    private String phrase;
    private String maskedPhrase;

    private int maxAttempts;
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public int getGameRoomSize() {
        return gameRoomSize;
    }

    public void setGameRoomSize(int gameRoomSize) {
        this.gameRoomSize = gameRoomSize;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setMaskedPhrase(String maskedPhrase) {
        this.maskedPhrase = maskedPhrase;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    private int currentPlayerIndex;


    public GameRoom( int gameRoomSize, int maxAttempts) {
        this.gameRoomSize = gameRoomSize;
        this.users = new ArrayList<>();
        this.maxAttempts = maxAttempts;
        initializePlayerIndex();
    }

    private void initializePlayerIndex() {
        currentPlayerIndex = 0;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase.toUpperCase();
        this.maskedPhrase = phrase.replaceAll("[A-Za-z]", "_");
    }

    public void addUser(User user) {
        users.add(user);
    }

    public boolean canStart() {
        return users.size() >= 2;
    }

    public boolean guessCharacter(char guessedChar) {
        guessedChar = Character.toUpperCase(guessedChar);
        boolean found = false;
        StringBuilder updatedMaskedPhrase = new StringBuilder(maskedPhrase);
        for (int i = 0; i < phrase.length(); i++) {
            if (phrase.charAt(i) == guessedChar) {
                updatedMaskedPhrase.setCharAt(i, guessedChar);
                found = true;
            }
        }
        if (!found) {
            users.get(currentPlayerIndex).decrementMaxAttempts();
        } else {
            users.get(currentPlayerIndex).incrementScore();
        }
        maskedPhrase = updatedMaskedPhrase.toString();
        return found;
    }

    public boolean isGameOver() {
        boolean allUsersHaveNoAttemptsLeft = true;
        for (User user : users) {
            if (user.getMaxAttempts() == 0) {
                allUsersHaveNoAttemptsLeft = false;
                break;
            }
        }
        return !maskedPhrase.contains("_") || !allUsersHaveNoAttemptsLeft;
    }

    public String nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % users.size();
        System.out.println("current player index: " + currentPlayerIndex);
        return getCurrentPlayer();
    }

    public String getCurrentPlayer() {
        return users.get(currentPlayerIndex).name;
    }

    public String getMaskedPhrase() {
        return maskedPhrase;
    }

    public List<User> getUsers() {
        return users;
    }

//    public User checkWinner() {
//        User winner = null;
//        int maxScore = -1;
//
//        for (User user : users) {
//            if (user.getScore() > maxScore) {
//                winner = user;
//                maxScore = user.getScore();
//            } else if (user.getScore() == maxScore) {
//                winner = null;
//            }
//        }
//
//        return winner;
//    }
    public ArrayList<User> getUsersOrderedByAttemptsLeft() {
        ArrayList<User> usersOrderedByAttemptsLeft = new ArrayList<>(users);

        for (int i = 0; i < usersOrderedByAttemptsLeft.size() - 1; i++) {
            for (int j = 0; j < usersOrderedByAttemptsLeft.size() - i - 1; j++) {
                User currentUser = usersOrderedByAttemptsLeft.get(j);
                User nextUser = usersOrderedByAttemptsLeft.get(j + 1);

                if (currentUser.getMaxAttempts() < nextUser.getMaxAttempts()) {
                    usersOrderedByAttemptsLeft.set(j, nextUser);
                    usersOrderedByAttemptsLeft.set(j + 1, currentUser);
                }
            }
        }

        return usersOrderedByAttemptsLeft;
    }

    public void addPlayerToGameRoom(User user) {
        int attempts= user.getMaxAttempts();
        user.setMaxAttempts(Integer.parseInt(Server.getConfigData()[0]));
        users.add(user);

    }

    public ArrayList<User> getPlayers() {
        return (ArrayList<User>) users;
    }

    public User getWinner() {
        ArrayList<User> arrangedUsers =  getUsersOrderedByAttemptsLeft();
        User winner = arrangedUsers.get(0);
        if (arrangedUsers.get(0).getMaxAttempts() == arrangedUsers.get(1).getMaxAttempts()) {
            winner = null;
        }
        return winner;
    }

    public String getAttemptsLeft() {
        return String.valueOf(users.get(currentPlayerIndex).getMaxAttempts());
    }
}
