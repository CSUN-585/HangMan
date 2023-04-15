import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * This class is used as an intermediate tracker/formatter
 * for the current game guess/word display
 */
public class GameWord {

    private final String goal;
    private final LinkedHashSet<Character> charSet;
    private final ObservableSet<Character> guessedSet;
    private final ObservableMap<Character, Boolean> guessMap;

    public GameWord(String input) {
        this.goal = input;
        this.charSet = new LinkedHashSet<>();
        this.guessedSet = FXCollections.observableSet();
        this.guessMap = FXCollections.observableMap(new HashMap<>());
        this.initCharSet(input);
    }

    private void initCharSet(String input) {
        for (Character c : input.toCharArray()) {
            charSet.add(c);
        }
        // now init our visibility map, all to false
        for (Character c : charSet) {
            guessMap.put(c, false);
        }
    }

    public boolean checkGuess(Character c) {
        // add the character to our history/guessed set
        guessedSet.add(c);
        if (charSet.contains(c)) {
            guessMap.put(c, true);
            return true;
        } else {
            return false;
        }
    }

    private boolean isVisible(Character c) {
        return charSet.contains(c) && guessMap.get(c);
    }

    public boolean isComplete() {
        for (Boolean val : guessMap.values()) {
            if (!val) {
                return false;
            }
        }
        // if we exit the loop, all values are true
        return true;
    }

    private String updateProgress() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < goal.length(); i++) {
            char c = goal.charAt(i);
            if (isVisible(c)) {
                sb.append(c);
            } else {
                sb.append("*");
            }
        }
        return sb.toString();
    }

    private boolean badGuess(Character c) {
        return !charSet.contains(c);
    }

    private String parseHistory() {
        StringJoiner sj = new StringJoiner(", ");
        guessedSet.stream()
                .filter(this::badGuess)
                .map(Objects::toString)
                .forEach(sj::add);
        return sj.toString();
    }

    public StringBinding progressProp() {
        return Bindings.createStringBinding(this::updateProgress, guessMap);
    }

    public StringBinding guessHistory() {
        return Bindings.createStringBinding(this::parseHistory, guessedSet);
    }

    public IntegerBinding lengthProp() {
        return Bindings.createIntegerBinding(goal::length, guessMap);
    }

}
