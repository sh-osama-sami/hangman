package server.model;

public class Score {
    String username;
    public int singleGameScoreWins;
    public int singleGameScoreLosses;

    public int multiGameScoreWins;
    public int multiGameScoreLosses;
    public int multiGameScoreDraws;
    public int GameRoomWins;
    public int GameRoomLosses;
    public int GameRoomDraws;



    public Score(String username) {
        this.username = username;
        this.singleGameScoreWins = 0;
        this.singleGameScoreLosses = 0;
        this.multiGameScoreWins = 0;
        this.multiGameScoreLosses = 0;
        this.multiGameScoreDraws = 0;
        this.GameRoomWins = 0;
        this.GameRoomLosses = 0;
        this.GameRoomDraws = 0;
    }

    public Score(String username, int parseInt, int parseInt1, int parseInt2, int parseInt3, int parseInt4, int parseInt5, int parseInt6, int parseInt7) {
        this.username = username;
        this.singleGameScoreWins = parseInt;
        this.singleGameScoreLosses = parseInt1;
        this.multiGameScoreWins = parseInt2;
        this.multiGameScoreLosses = parseInt3;
        this.multiGameScoreDraws = parseInt4;
        this.GameRoomWins = parseInt5;
        this.GameRoomLosses = parseInt6;
        this.GameRoomDraws = parseInt7;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
