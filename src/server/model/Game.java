package server.model;

import server.model.Team;
import server.model.User;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<Team> teams;
    private String phrase;
    private List<String> maskedPhrases;
    private int maxAttempts;

    private int currentPlayerIndex;
    private int currentTeamIndex;

    public Game(int maxAttempts) {
        this.teams = new ArrayList<>();
        this.maskedPhrases = new ArrayList<>();
        this.maxAttempts = maxAttempts;
        initializePlayerAndTeamIndices();
    }


    public Game(int maxAttempts , ArrayList<Team> teams) {
        this.teams = teams;
        this.maxAttempts = maxAttempts;
        this.maskedPhrases = new ArrayList<>();
        initializePlayerAndTeamIndices();
    }

    private void initializePlayerAndTeamIndices() {
        currentPlayerIndex = 0;
        currentTeamIndex = 0;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase.toUpperCase();
        for (int i = 0; i < teams.size(); i++) {
            maskedPhrases.add(phrase.replaceAll("[A-Za-z]", "_"));
        }
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
        StringBuilder updatedMaskedPhrase = new StringBuilder(maskedPhrases.get(currentTeamIndex));
        for (int i = 0; i < phrase.length(); i++) {
            if (phrase.charAt(i) == guessedChar) {
                updatedMaskedPhrase.setCharAt(i, guessedChar);
                found = true;
            }
        }
        maskedPhrases.set(currentTeamIndex, updatedMaskedPhrase.toString());
        return found;
    }

    public boolean isGameOver() {
        return !maskedPhrases.get(currentTeamIndex).contains("_");
    }

    public String nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % teams.get(currentTeamIndex).getPlayers().size();
        if (currentPlayerIndex == 0) {
            currentTeamIndex = (currentTeamIndex + 1) % teams.size();
        }
        return getCurrentPlayer();

    }

    public String getCurrentPlayer() {
        return teams.get(currentTeamIndex).getPlayers().get(currentPlayerIndex).name;
    }

    public String getMaskedPhrase() {
        return maskedPhrases.get(currentTeamIndex);
    }

    public List<Team> getTeams() {
        return teams;
    }
}
