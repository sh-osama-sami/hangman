package server;

import java.io.BufferedReader;
import java.io.IOException;

public class lookupListener {

    static BufferedReader serverInput = null;
    public boolean end = false;



    public lookupListener(BufferedReader serverInput) {
        this.serverInput = serverInput;
    }

    public static String getWordFromServer(){
        String word="";
        String lookupserverinput = null;
        try {
            lookupserverinput = serverInput.readLine();
            if (lookupserverinput.startsWith("/RANDOM_WORD")) {
                 word = lookupserverinput.split(":")[1];
                System.out.println("random word from server: " + word);
                return word;
//                    clientOutput.println("RANDOM_WORD:" + word);
            }
        } catch (IOException e) {
            System.out.println("Error :( : " + e);
        }

        return word;
    }





}
