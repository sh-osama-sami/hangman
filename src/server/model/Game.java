package server.model;

public class Game {
    public static int numberofAttempts;
    public static int minNumberOfPlayersInTeam;
    public static int maxNumberOfPlayersInTeam;
    String phrase;

    public Game(int numberofAttempts, int minNumberOfPlayersInTeam, int maxNumberOfPlayersInTeam) {
        this.numberofAttempts = numberofAttempts;
        this.minNumberOfPlayersInTeam = minNumberOfPlayersInTeam;
        this.maxNumberOfPlayersInTeam = maxNumberOfPlayersInTeam;
    }

    public int getNumberofAttempts() {
        return numberofAttempts;
    }

    public void setNumberofAttempts(int numberofAttempts) {
        this.numberofAttempts = numberofAttempts;
    }

    public int getMinNumberOfPlayersInTeam() {
        return minNumberOfPlayersInTeam;
    }

    public void setMinNumberOfPlayersInTeam(int minNumberOfPlayersInTeam) {
        this.minNumberOfPlayersInTeam = minNumberOfPlayersInTeam;
    }

    public int getMaxNumberOfPlayersInTeam() {
        return maxNumberOfPlayersInTeam;
    }

    public void setMaxNumberOfPlayersInTeam(int maxNumberOfPlayersInTeam) {
        this.maxNumberOfPlayersInTeam = maxNumberOfPlayersInTeam;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}
