package server.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private static final String USERS = "users.txt";
    private static final String TEAMS = "teams.txt";

    private static final String SCORE = "score.txt";
    private static final String GAMECONFIG = "gameconfig.txt";
    private static final String PHRASES = "lookup.txt";


    public static ArrayList<User> loadUsersFromFile() {
        ArrayList<User> users = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(USERS));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                String email = parts[2];
                String password = parts[3];

                User user = new User( name, email, password);
                users.add(user);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }
    public static void saveUser(User user) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USERS, true));
            writer.write(user.getId() + "," + user.getName() + "," + user.getUsername() + "," + user.getPassword() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(USERS));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                String email = parts[2];
                String password = parts[3];

                User user = new User( name, email, password);
                users.add(user);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }
    public static User loadUserFromFile(String username) throws IOException {

        System.out.println("fileName: " + USERS  + " username: " + username
        );
        File file = new File(USERS);
        System.out.println("file: " + file);

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        System.out.println("reader: " + reader  );
        String line;


        while ((line = reader.readLine()) != null) {
            System.out.println("line: " + line);
            String[] userData = line.split(",");
            if (userData[2].equals(username)) {
                int id = Integer.parseInt(userData[0]);
                User user = new User(id, userData[1],userData[2],userData[3]);
                reader.close();
                return user;
            }

        }

        reader.close();
        return null;
    }

//    public static void saveTeam(Team team) throws IOException {
//        File file = new File(TEAMS);
//        System.out.println("file: " + file);
//
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(TEAMS, true));
//            writer.write(team.getId() + "," + team.getName() +  "," + team.getScore() + "," + team.);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    public static Team loadTeamFromFile(String teamName) throws IOException {
//
//        System.out.println("fileName: " + TEAMS + " teamName: " + teamName
//        );
//        File file = new File(TEAMS);
//        System.out.println("file: " + file);
//
//
//
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        System.out.println("reader: " + reader);
//        String line;
//
//        while ((line = reader.readLine()) != null) {
//            System.out.println("line: " + line);
//            String[] teamData = line.split(",");
//            if (teamData[1].equals(teamName)) {
//                int id = Integer.parseInt(teamData[0]);
//                Team team = new Team(id, teamData[1], Integer.parseInt(teamData[2]));
//                reader.close();
//                return team;
//            }
//
//        }
//    }


    public static void saveScore(Score score) throws IOException {
        File file = new File(SCORE);
        System.out.println("file: " + file);

        if (!file.exists()) {
            file.createNewFile();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE, true));
            writer.write( score.getUsername() +  "," + score.singleGameScoreWins +"," + score.singleGameScoreLosses
                    + "," + score.multiGameScoreWins + "," + score.multiGameScoreLosses + "," + score.multiGameScoreDraws
                    + "," + score.GameRoomWins + "," + score.GameRoomLosses + "," + score.GameRoomDraws
                    + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteScore(Score score) throws IOException {
        File file = new File(SCORE);
        File tempFile = new File("temp_" + SCORE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] scoreData = line.split(",");
                if (!scoreData[0].equals(score.getUsername())) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        if (!file.delete()) {
            throw new IOException("Failed to delete original score file");
        }
        if (!tempFile.renameTo(file)) {
            throw new IOException("Failed to rename temp file to original score file");
        }
    }

    public static void updateScore(Score score) throws IOException {
        deleteScore(score);
        saveScore(score);
    }

    public static ArrayList<Score> loadAllScoresFromFileToServer() throws IOException {
        System.out.println("fileName: " + SCORE);
        File file = new File(SCORE);
        System.out.println("file: " + file);

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        System.out.println("reader: " + reader  );
        String line;
        ArrayList<Score> scores = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            System.out.println("line: " + line);
            String[] socreData = line.split(",");
            Score score = new Score(socreData[0], Integer.parseInt(socreData[1]), Integer.parseInt(socreData[2]), Integer.parseInt(socreData[3]),
                    Integer.parseInt(socreData[4]), Integer.parseInt(socreData[5]), Integer.parseInt(socreData[6]),
                    Integer.parseInt(socreData[7]), Integer.parseInt(socreData[8]));
            scores.add(score);
        }

        reader.close();
        return scores;

    }

    public static Score loadUserScoreFromFile(String username) throws IOException {
        System.out.println("fileName: " + SCORE  + " username: " + username
        );
        File file = new File(SCORE);
        System.out.println("file: " + file);

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        System.out.println("reader: " + reader  );
        String line;


        while ((line = reader.readLine()) != null) {
            System.out.println("line: " + line);
            String[] socreData = line.split(",");
            if (socreData[0].equals(username)) {
                Score score = new Score(socreData[0], Integer.parseInt(socreData[1]), Integer.parseInt(socreData[2]), Integer.parseInt(socreData[3]),
                        Integer.parseInt(socreData[4]), Integer.parseInt(socreData[5]), Integer.parseInt(socreData[6]),
                        Integer.parseInt(socreData[7]), Integer.parseInt(socreData[8]));
                reader.close();
                return score;
            }

        }

        reader.close();
        return null;
    }
    public static String[] loadGameConfigFromFile() throws IOException {

        File file = new File(GAMECONFIG);
        System.out.println("file: " + file);

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        System.out.println("reader: " + reader  );
        String line;


        while ((line = reader.readLine()) != null) {
            System.out.println("line: " + line);
            String[] gameData = line.split(",");

                reader.close();
                return gameData;


        }

        reader.close();
        return null;
    }
    public static ArrayList<String> loadLookUpFile(){
        ArrayList<String> phrases = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(PHRASES));
            String line;

            while ((line = reader.readLine()) != null) {
                phrases.add(line);
            }

            reader.close();
            return phrases;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return phrases;
    }
}
