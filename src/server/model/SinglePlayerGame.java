package server.model;



    public class SinglePlayerGame
    {   static public User user;
        static  public int id = 0;
        private String phrase;
        private String maskedPhrase;

        public String getPhrase() {
            return phrase;
        }

        public void setMaskedPhrase(String maskedPhrase) {
            this.maskedPhrase = maskedPhrase;
        }

        public void setRemainingAttempts(int remainingAttempts) {
            this.remainingAttempts = remainingAttempts;
        }

        private int maxAttempts;
        private int remainingAttempts;

        public SinglePlayerGame(String phrase, String maxAttempts,User user) {
            this.user = user;
            SinglePlayerGame.id = id+1;
            setPhrase(phrase);
            this.maxAttempts = Integer.parseInt(maxAttempts);
            this.remainingAttempts = Integer.parseInt(maxAttempts);
        }

        public void setPhrase(String phrase) {
            this.phrase = phrase.toUpperCase();
            this.maskedPhrase = phrase.replaceAll("[A-Za-z]", "_");
        }

        public static User getUser() {
            return user;
        }

        public static void setUser(User user) {
            SinglePlayerGame.user = user;
        }

        public boolean guessCharacter(char guessedChar) {
            guessedChar = Character.toUpperCase(guessedChar);
            boolean found = false;
            StringBuilder updatedMaskedPhrase = new StringBuilder(maskedPhrase);
            for (int i = 0; i < phrase.length(); i++) {
                if (phrase.charAt(i) == guessedChar) {
                    updatedMaskedPhrase.setCharAt(i, guessedChar);
                    found = true;
                }
            }
            maskedPhrase = updatedMaskedPhrase.toString();
            if (!found) {
                remainingAttempts--;
                return false;
            }
            return true;
        }

        public boolean isGameOver() {
            System.out.println("maskedPhrase: " + maskedPhrase);
            System.out.println("remainingAttempts: " + remainingAttempts);
            return !maskedPhrase.contains("_") || remainingAttempts == 0;
        }

        public String getMaskedPhrase() {
            return maskedPhrase;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }

        public boolean hasWon() {
            return !maskedPhrase.contains("_");
        }
    }


