package server.model;

import server.model.Team;
import server.model.User;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<Team> teams;
    private String phrase;
    private String maskedPhrase;
    private int maxAttempts;

    private int currentPlayerIndex;
    private int currentTeamIndex;

    public Game(int maxAttempts) {
        this.teams = new ArrayList<>();
        this.maxAttempts = maxAttempts;
    }

    public Game(int maxAttempts , ArrayList<Team> teams) {
        this.teams = teams;
        this.maxAttempts = maxAttempts;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase.toUpperCase();
        this.maskedPhrase = phrase.replaceAll("[A-Za-z]", "_");
    }

    public void addTeam(Team team) {
        teams.add(team);
    }

    public boolean canStart() {
        if (teams.size() < 2) return false;
        int team1Size = teams.get(0).getNumberOfPlayers();
        int team2Size = teams.get(1).getNumberOfPlayers();
        return team1Size == team2Size;
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
        maskedPhrase = updatedMaskedPhrase.toString();
        return found;
    }

    public boolean isGameOver() {
        return !maskedPhrase.contains("_");
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % teams.get(currentTeamIndex).getPlayers().size();
        if (currentPlayerIndex == 0) {
            currentTeamIndex = (currentTeamIndex + 1) % teams.size();
        }
    }

    public User getCurrentPlayer() {
        return teams.get(currentTeamIndex).getPlayers().get(currentPlayerIndex);
    }

    public String getMaskedPhrase() {
        return maskedPhrase;
    }

    public List<Team> getTeams() {
        return teams;
    }
}
