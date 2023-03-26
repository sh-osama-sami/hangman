package lookup_server;

import server.model.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;


public class LookupClientThread extends Thread{
    BufferedReader clientInput = null;
    PrintStream clientOutput = null;
    Socket communicationSocket = null;
    public LookupClientThread(Socket clientSocket) {
        communicationSocket = clientSocket;
    }
    public void run() {


        try {

            //Initializing I/O streams
            clientInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
            clientOutput = new PrintStream(communicationSocket.getOutputStream());

            while (true) {
                String input = clientInput.readLine();

                //getting a random word from the lookup file
                if (input.equals("/RANDOM_WORD")) {
                    String response = getRandomWord();
                    clientOutput.println("/RANDOM_WORD:"+response);
                }
            }
        } catch (IOException e) {
            System.out.println( "client disconnected.");

        }
    }
    public static String getRandomWord() {
        try {
            String randomWord = LookUpServer.phrases.get((int) (Math.random() * LookUpServer.phrases.size()));
            return randomWord;
        }catch (Exception e){
            System.out.println("Error: " + e);
        }
        return "";
    }
}