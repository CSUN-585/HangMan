public enum GameStatus {
    GAME_OVER("Game over!"),
    BAD_GUESS("Bad guess..."),
    GOOD_GUESS("Good guess!"),
    WON("You won!"),
    OPEN("Game on, let's go!");

    private String text;
    GameStatus(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}