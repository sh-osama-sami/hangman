package server.model;

import server.model.Team;
import server.model.User;

import java.util.ArrayList;
import java.util.List;

public class Game2 {
    private List<Team> teams;
    private String phrase;

    private int maxAttempts;


    private int currentPlayerIndex;
    private int currentTeamIndex;

    public Game2(int maxAttempts) {
        this.teams = new ArrayList<>();

        this.maxAttempts = maxAttempts;
        initializePlayerAndTeamIndices();
    }


    public Game2(int maxAttempts , ArrayList<Team> teams) {
        this.teams = teams;
        this.maxAttempts = maxAttempts;

        initializePlayerAndTeamIndices();
    }

    private void initializePlayerAndTeamIndices() {
        currentPlayerIndex = 0;
        currentTeamIndex = 0;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase.toUpperCase();
        String maskedPhrase = phrase.replaceAll("[A-Za-z]", "_");
        for (Team team : teams) {
            team.setTeamMaskedPhrase(maskedPhrase);
        }
        for (Team team : teams) {
            System.out.println("team name and masked phrase: " + team.getName() + " " + team.getTeamMaskedPhrase());
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
        System.out.println("current Player  and his team and his masked word:" + getCurrentPlayer() + " " + currentTeamIndex + " " + teams.get(currentTeamIndex).getTeamMaskedPhrase() );
        guessedChar = Character.toUpperCase(guessedChar);
        boolean found = false;
        Team currentTeam = teams.get(currentTeamIndex);
        StringBuilder updatedMaskedPhrase = new StringBuilder(currentTeam.getTeamMaskedPhrase());
        for (int i = 0; i < phrase.length(); i++) {
            if (phrase.charAt(i) == guessedChar) {
                updatedMaskedPhrase.setCharAt(i, guessedChar);
                found = true;
            }
        }
        currentTeam.setTeamMaskedPhrase(updatedMaskedPhrase.toString());
        System.out.println("current Player  and his team and his masked word:" + getCurrentPlayer() + " " + currentTeamIndex + " " + teams.get(currentTeamIndex).getTeamMaskedPhrase() );
        return found;
    }

    public boolean isGameOver() {
        return ((!teams.get(currentTeamIndex).getTeamMaskedPhrase().contains("_")) && (teams.get(0).getCurrentAttempt() == teams.get(1).getCurrentAttempt()) || ((teams.get(0).getCurrentAttempt() == maxAttempts) && (teams.get(1).getCurrentAttempt() == maxAttempts)));
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
        return teams.get(currentTeamIndex).getTeamMaskedPhrase();
    }

    public List<Team> getTeams() {
        return teams;
    }
}
