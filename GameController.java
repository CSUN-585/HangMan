import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

public class GameController {

	private final ExecutorService executorService;
	private final Game game;
	private HashSet<Shape> glyphParts;
	private final Stack<Shape> glyphStack;
	
	public GameController(Game game) {
		this.game = game;
		this.glyphStack = new Stack<>();
		executorService = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			return thread;
		});
	}

	@FXML
	private VBox board;
	@FXML
	private Label statusLabel, enterALetterLabel, answerLabel, countLabel, movesLabel, guessHistory;
	@FXML
	private TextField textField ;
	@FXML
	private MenuBar gameMenuBar;
	@FXML
	private Line glyphTorso, glyphArmLeft, glyphArmRight, glyphLegLeft, glyphLegRight;
	@FXML
	private Circle glyphHead;

    public void initialize() throws IOException {
		System.out.println("in initialize");
		final String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Mac"))
			gameMenuBar.useSystemMenuBarProperty().set(true);
		// initialize our glyph parts/shapes
		this.glyphParts = initGlyphSet();
		addTextBoxListener();
		setUpStatusLabelBindings();
		// now by default, start a new game on load
		newHangman();
	}

	private HashSet<Shape> initGlyphSet() {
		HashSet<Shape> parts = new LinkedHashSet<>();
		parts.add(glyphHead);
		parts.add(glyphTorso);
		parts.add(glyphArmLeft);
		parts.add(glyphArmRight);
		parts.add(glyphLegLeft);
		parts.add(glyphLegRight);
		return parts;
	}

	private void initGlyphStack() {
		glyphStack.push(glyphLegRight);
		glyphStack.push(glyphLegLeft);
		glyphStack.push(glyphArmRight);
		glyphStack.push(glyphArmLeft);
		glyphStack.push(glyphTorso);
		glyphStack.push(glyphHead);
	}

	/**
	 * Resets the visibility of the component shapes
	 * used to draw our hangman graphic/tracker
	 */
	private void resetGlyph() {
		for (Shape s : glyphParts) {
			s.setVisible(false);
		}
		initGlyphStack();
	}

	private void addTextBoxListener() {
		textField.textProperty().addListener((ov, oldValue, newValue) -> {
			if(newValue.length() > 0 && !glyphStack.isEmpty()) {
				System.out.print(newValue);
				game.makeMove(newValue);
				textField.clear();
			} else {
				// game is over, so just clear the text field
				textField.clear();
			}
		});
	}

	private void setUpStatusLabelBindings() {
		System.out.println("in setUpStatusLabelBindings");
		statusLabel.textProperty().bind(Bindings.format("%s", game.gameStatusProperty()));
		enterALetterLabel.textProperty().bind(Bindings.format("%s", "Enter a letter:"));
		// add our listener to update the hangman glyph on bad guesses
		game.movesProp().addListener((obs, oldVal, newVal) -> guessListener(oldVal, newVal));
		answerLabel.textProperty().bind(game.progressProp());
		countLabel.textProperty().bind(
				Bindings.createStringBinding(() -> game.lengthProp().get() + " letters", game.lengthProp())
		);
		guessHistory.textProperty().bind(game.historyProp());
		movesLabel.textProperty().bind(
				Bindings.createStringBinding(() -> "guesses remaining: " + game.movesProp().get(), game.movesProp())
		);
	}

	private void guessListener(Number oldVal, Number newVal) {
		if (newVal.intValue() < oldVal.intValue()) {
			if (glyphStack.isEmpty()) {
				System.out.println("Glyph stack empty; out of moves!");
			} else {
				// pop the next 'limb' from our stack and toggle visibility
				glyphStack.pop().setVisible(true);
			}
		}
	}

	@FXML
	private void displayAboutBox() {
		Alert alert = new SkinnedAlert(Alert.AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("Comp 585 - Project 3, HangMan!");
		alert.setContentText("Jocelyn Mallon,\n" +
				"Miranda Medina,\n" +
				"Hemanth Illuri &\n" +
				"Mackayla Rodriguez");
		alert.showAndWait();
	}

	@FXML 
	private void newHangman() {
		// reset our game 'graphics'
		resetGlyph();
		// now tell the game object to reset
		game.reset();
	}

	@FXML
	private void quit() {
		board.getScene().getWindow().hide();
	}
}