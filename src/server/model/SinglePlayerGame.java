package server.model;



    public class SinglePlayerGame {
        static  public int id = 0;
        private String phrase;
        private String maskedPhrase;
        private int maxAttempts;
        private int remainingAttempts;

        public SinglePlayerGame( String phrase, int maxAttempts) {
            SinglePlayerGame.id = id+1;
            setPhrase(phrase);
            this.maxAttempts = maxAttempts;
            this.remainingAttempts = maxAttempts;
        }

        public void setPhrase(String phrase) {
            this.phrase = phrase.toUpperCase();
            this.maskedPhrase = phrase.replaceAll("[A-Za-z]", "_");
        }

        public String guessCharacter(char guessedChar) {
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
                return "WRONG";
            }
            return "CORRECT";
        }

        public boolean isGameOver() {
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


