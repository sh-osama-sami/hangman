package server.model;

public class User {
    static int id ;
    String username;
    String name;
    String password;
    Team team;
    int noOfAttempts;
    int GameRoomScore = 0;


    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    Score score;

    public User(String name, String username, String password) {
        generateId();
        this.username = username;
        this.name = name;
        this.password = password;
    }
    public User(int id ,String name , String username, String password) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void generateId(){
            this.id++;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void decrementMaxAttempts() {
        this.noOfAttempts--;
    }

    public void incrementScore() {
        this.GameRoomScore++;
    }

    public int getMaxAttempts() {
        return this.noOfAttempts;
    }

    public void setMaxAttempts(int attempts) {
        this.noOfAttempts = attempts;
    }
}
