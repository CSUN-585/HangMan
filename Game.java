import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Game {

	private String[] words;
	private GameWord currentWord;
	private final StringProperty answerProgress;
	private final StringProperty guessHistory;
	private final IntegerProperty answerLength;
	private final IntegerProperty movesLeft;
	private final BooleanProperty lastGuess;
	private final BooleanProperty resetFlag;
	private final ReadOnlyObjectWrapper<GameStatus> gameStatus;
	private final ObjectProperty<Boolean> gameState = new ReadOnlyObjectWrapper<>();

	public Game() {
		answerProgress = new SimpleStringProperty();
		guessHistory = new SimpleStringProperty();
		answerLength = new SimpleIntegerProperty();
		lastGuess = new SimpleBooleanProperty(false);
		resetFlag = new SimpleBooleanProperty(false);
		movesLeft = new SimpleIntegerProperty(6);
		gameStatus = new ReadOnlyObjectWrapper<>(this, "gameStatus", GameStatus.OPEN);
		initWordsArray();
		gameState.setValue(false); // initial state
		reset();
		createGameStatusBinding();
	}

	public StringProperty progressProp() {
		return this.answerProgress;
	}

	public IntegerProperty lengthProp() {
		return this.answerLength;
	}

	public IntegerProperty movesProp() {
		return movesLeft;
	}

	public StringProperty historyProp() {
		return guessHistory;
	}

	public ObjectProperty<GameStatus> gameStatusProperty() {
		return gameStatus;
	}

	private void createGameStatusBinding() {
		ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<>() {
			{
				super.bind(gameState);
			}

			@Override
			public GameStatus computeValue() {
				log("in computeValue");
				if (resetFlag.get()) {
					resetFlag.set(false);
					return GameStatus.OPEN;
				} else if (currentWord.isComplete() && !outOfMoves()) {
					return GameStatus.WON;
				} else {
					if (lastGuess.get()) {
						return GameStatus.GOOD_GUESS;
					} else {
						decrementMoves();
						if (outOfMoves()) {
							return GameStatus.GAME_OVER;
						} else {
							return GameStatus.BAD_GUESS;
						}
					}
				}
			}
		};
		gameStatus.bind(gameStatusBinding);
	}

	private boolean outOfMoves() {
		return movesLeft.get() == 0;
	}

	private void decrementMoves() {
		if (movesLeft.get() > 0) {
			movesLeft.set(movesLeft.get() - 1);
		}
	}

	private void initWordsArray() {
		if (words == null || words.length == 0) {
			try {
				words = Files.readAllLines(Paths.get("words.txt")).toArray(new String[0]);
			} catch (IOException e) {
				log("Error when loading words.txt");
				throw new RuntimeException(e);
			}
		}
	}

	private void setRandomWord() {
		int idx = (int) (Math.random() * words.length);
		String answer = words[idx].trim().toLowerCase();
		if (answer.isEmpty() || answer.isBlank()) {
			log("blank/empty word chosen, re-rolling");
			setRandomWord();
		} else {
			log("word chosen: " + answer);
			currentWord = new GameWord(answer);
			answerProgress.bind(currentWord.progressProp());
			answerLength.bind(currentWord.lengthProp());
			guessHistory.bind(currentWord.guessHistory());
		}
	}

	public void makeMove(String letter) {
		log("\nin makeMove: " + letter);
		// this will update and toggle the state of the game
		lastGuess.set(currentWord.checkGuess(letter.charAt(0)));
		gameState.setValue(!gameState.getValue());
	}

	public void reset() {
		setRandomWord();
		resetFlag.set(true);
		movesLeft.set(6);
		lastGuess.set(false);
		gameState.setValue(!gameState.getValue());
	}

	public static void log(String s) {
		System.out.println(s);
	}
}
