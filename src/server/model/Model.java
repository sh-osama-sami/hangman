package server.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private static final String FILENAME = "users.txt";

    public static void saveUser(User user) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME, true));
            writer.write(user.getId() + "," + user.getName() + "," + user.getUsername() + "," + user.getPassword() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
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
        String fileName = "users.txt";
        File file = new File(fileName);

        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if (userData[2].equals(username)) {
                int id = Integer.parseInt(userData[0]);
                User user = new User(id, userData[1],userData[2],userData[3]);
                reader.close();
                return user;
            }
            else return null;
        }

        reader.close();
        return null;
    }
}
