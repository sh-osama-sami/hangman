package server.model;

public class Score {
    String username;
    int singleGameScore;
    int multiGameScore;
    int totalScore;

    public Score(String username, int singleGameScore, int multiGameScore, int totalScore) {
        this.username = username;
        this.singleGameScore = singleGameScore;
        this.multiGameScore = multiGameScore;
        this.totalScore = totalScore;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getSingleGameScore() {
        return singleGameScore;
    }

    public void setSingleGameScore(int singleGameScore) {
        this.singleGameScore = singleGameScore;
    }

    public int getMultiGameScore() {
        return multiGameScore;
    }

    public void setMultiGameScore(int multiGameScore) {
        this.multiGameScore = multiGameScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}
