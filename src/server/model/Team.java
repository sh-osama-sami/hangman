package server.model;

public class Team {
    private String name;
    private static int  id = 0;
    private int score;
    private int wins;
    private int losses;
    private int draws;

    public Team(String name) {
        this.name = name;
        Team.id = id+1;
        this.score = 0;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }
}
