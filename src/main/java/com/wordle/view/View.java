package com.wordle.view;

import com.wordle.controller.Controller;
import com.wordle.model.Model;
import com.wordle.model.Observer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Duration;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;

public class View implements FXComponent, Observer {
    private final Controller controller;
    private final Model model;
    private final Stage stage;
    private boolean musicStart = false;
    private final Music music;

    public View(Controller controller, Model model, Stage stage) {
        this.controller = controller;
        this.model = model;
        this.stage = stage;
        this.music = new Music();
    }

    public Parent render() {
        StackPane root = new StackPane();
        BorderPane pane = new BorderPane();
        VBox header = renderHeader();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 0, 0, 0));
        pane.setTop(header);

        pane.getStyleClass().add("main-background");
        pane.setCenter(renderGrid());

        VBox bottom = new VBox(15);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(0, 0, 40, 0));

        if(model.canUseHint()){
            Button hintButton = new Button("REVEAL HINT");
            hintButton.setStyle("-fx-background-color: #6aaa64; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
            hintButton.setOnAction(e -> {
                controller.useHint();
            });
            VBox.setMargin(hintButton, new Insets(0, 0, 20, 0));
            bottom.getChildren().add(hintButton);
        }
        if(model.getModeStatus() != Model.MODE.HARD){
            VBox keyboard = renderKeyboard();
            keyboard.setAlignment(Pos.CENTER);
            keyboard.setPadding(new Insets(0));
            bottom.getChildren().add(keyboard);
        }
        else{
            Label noKeyboard = new Label("KEYBOARD DISABLED");
            noKeyboard.setStyle(
                    "-fx-font-weight: bold; -fx-font-size: 25px;");
            noKeyboard.setPadding(new Insets(0,0, 125, 0));
            bottom.getChildren().add(noKeyboard);

        }
        pane.setBottom(bottom);
        root.getChildren().add(pane);
        if(model.getStatus() == Model.STATUS.END_GAME){
            List<String> guesses = model.getGuesses();
            boolean win = !guesses.isEmpty() && guesses.get(guesses.size()-1).equals(model.getTargetWord());

            String gameOverText = "";
            String buttonText="";
            String revealWord = "";
            if(win){
                 gameOverText = "You Won!";
                 buttonText = "Play Again";
            }
            else{
                gameOverText = "Game Over!";
                buttonText = "Try Again";
                revealWord = model.getTargetWord();
            }
            Label gameOver = new Label(gameOverText);
            gameOver.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
            Button restart = new Button(buttonText);
            restart.setOnAction(e -> controller.getTargetWord());
            restart.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 20px; -fx-padding: 10 20;");
            Label reveal = new Label("The word was: " + revealWord);
            reveal.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 20px; -fx-text-fill: #aaa;");

            VBox results = new VBox(10);
            results.getChildren().add(gameOver);
            if(revealWord != ""){
                results.getChildren().add(reveal);
            }
            results.getChildren().add(restart);
            results.setAlignment(Pos.CENTER);
            results.setStyle("-fx-background-color: rgba(30, 30, 30, 0.85); ");
            root.getChildren().add(results);

        }
        return root;
    }

    private void buttonStyle(Button b, Model.MODE m, boolean isActive) {
        String baseStyle = "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15; ";
        if (isActive) {
            b.setStyle(baseStyle + "-fx-background-color: #538d4e; -fx-text-fill: white; -fx-border-color: #538d4e;");
            if(m == Model.MODE.EASY){
                b.setStyle(baseStyle + "-fx-background-color: #6aaa64; -fx-text-fill: white; -fx-border-color: #6aaa64;");
            }
            if(m == Model.MODE.MEDIUM){
                b.setStyle(baseStyle + "-fx-background-color: #c9b458; -fx-text-fill: white; -fx-border-color: #c9b458;");
            }
            if(m == Model.MODE.HARD){
                b.setStyle(baseStyle + "-fx-background-color: #cc4d4d; -fx-text-fill: white; -fx-border-color: #cc4d4d;");
            }
        } else {
            b.setStyle(baseStyle + "-fx-background-color: #ffffff; -fx-text-fill: #787c7e; -fx-border-color: #d3d6da; -fx-border-width: 1;");
        }
        b.setOnMouseEntered(e -> {
            if (!isActive) b.setStyle(b.getStyle() + "-fx-background-color: #f8f8f8;");
        });
        b.setOnMouseExited(e -> {
            if (!isActive) b.setStyle(b.getStyle() + "-fx-background-color: #ffffff;");
        });
    }

    private VBox renderHeader(){
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        HBox difficulty = new HBox(10);
        difficulty.setAlignment(Pos.CENTER);
        for(Model.MODE m : Model.MODE.values()){
            Button b = new Button(m.toString());
            boolean activeButton = false;
            if(model.getModeStatus() == m){
                activeButton = true;
            }
            buttonStyle(b, m, activeButton);
            b.setOnAction(e -> {
                model.setModeStatus(m);
                controller.getTargetWord();
            });
            difficulty.getChildren().add(b);
        }
        header.getChildren().add(difficulty);

        return header;
    }

    private GridPane renderGrid(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);

        int hintIndex = model.getHintIndex();
        List<String> guesses = model.getGuesses();
        String input = controller.getInput();
        int rows = model.getMaxAttempts();

        for(int r=0; r<rows; r++){
            String[] rowStyles = null;
            if(r<guesses.size()){
                rowStyles = getRowStyles(guesses.get(r));
            }
            for(int c=0; c<5; c++){
                StackPane tile = new StackPane();
                tile.setMinSize(60, 60);
                tile.setPrefSize(60, 60);
                tile.setMaxSize(60, 60);
                tile.setStyle("-fx-border-color: #d3d6da; -fx-border-width: 2; -fx-background-color: white;");
                Label l = new Label();
                l.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");

                String bgColor = "white";
                String borderColor = "#d3d6da";
                String textColor = "black";

                if(r < guesses.size()){
                    String guess = guesses.get(r);
                    l.setText(String.valueOf(guess.charAt(c)));
                    textColor = "white";

                    if(rowStyles[c].equals("tile-correct")){
                        bgColor= "#6aaa64";
                        borderColor = "#6aaa64";
                    } else if(rowStyles[c].equals("tile-present")){
                        bgColor= "#c9b458";
                        borderColor = "#c9b458";
                    } else{
                        bgColor = "#787c7e";
                        borderColor = "#787c7e";
                    }
                }
                else if(r == guesses.size()){
                    if(c<input.length()){
                        l.setText(String.valueOf(input.charAt(c)));
                        borderColor = "#878a8c";
                    }
                    else if(c == hintIndex){
                        l.setText(String.valueOf(model.getTargetWord().charAt(c)));
                        borderColor = "#6aaa64";
                        textColor = "#6aaa64";
                        tile.setOpacity(0.7);
                    }
                }
                tile.setStyle("-fx-background-color: " + bgColor + "; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 2;");
                l.setStyle(l.getStyle() + "-fx-text-fill: " + textColor + ";");
                tile.getChildren().add(l);
                grid.add(tile, c, r);
            }
        }
        return grid;
    }

    private String[] getRowStyles(String guess){
        String target = model.getTargetWord();
        String[] styles = new String[5];
        List<Character> letters = new ArrayList<>();

        for(int i=0; i<5; i++){
            letters.add(target.charAt(i));
        }

        for(int i=0; i<5; i++){
            if(guess.charAt(i) == target.charAt(i)){
                styles[i] = "tile-correct";
                letters.remove(Character.valueOf(guess.charAt(i)));
            }
        }

        for(int i=0; i<5; i++){
            if(styles[i] == null){
                if(letters.contains(guess.charAt(i))){
                    styles[i] = "tile-present";
                    letters.remove(Character.valueOf(guess.charAt(i)));
                }
                else{
                    styles[i] = "tile-absent";
                }
            }
        }
        return styles;
    }

    private VBox renderKeyboard(){
        VBox keyboard = new VBox(8);
        keyboard.setAlignment(Pos.CENTER);
        String[] keyRows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        String baseStyle = "-fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 14px;";

        for(int i=0; i<keyRows.length; i++){
            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER);
            String rowLetters = keyRows[i];

            for(int j=0; j<rowLetters.length(); j++){
                char c = rowLetters.charAt(j);
                Button key = new Button(String.valueOf(c));
                key.setPrefSize(45, 55);
                key.setStyle(baseStyle + getKeyColor(c));
                key.setOnAction(e -> controller.processKeyPress(String.valueOf(c)));
                row.getChildren().add(key);
            }

            if (i == 0) {
                Button enter = new Button("⟵");
                enter.setPrefSize(65, 58);
                enter.setStyle(baseStyle + "-fx-background-color: #d3d6da; -fx-text-fill: black;");
                enter.setOnAction(e -> controller.processKeyPress("BACK_SPACE"));
                row.getChildren().add(enter);
            }

            if (i == 2) {
                Button back = new Button("ENTER");
                back.setPrefSize(65, 58);
                back.setStyle(baseStyle + "-fx-background-color: #d3d6da; -fx-text-fill: black;");
                back.setOnAction(e -> controller.processKeyPress("ENTER"));
                row.getChildren().add(back);
            }

            keyboard.getChildren().add(row);
        }
        return keyboard;
    }

    private String getKeyColor(char c){
        String in = String.valueOf(c);
        String word = model.getTargetWord();
        List<String> guesses = model.getGuesses();
        String color = "-fx-background-color: #d3d6da; -fx-text-fill: black;";

        if (word == null || word.isEmpty()) {
            return "-fx-background-color: #d3d6da; -fx-text-fill: black;";
        }

        for (String guess: guesses){
            for(int i=0; i<guess.length(); i++){
                if(guess.charAt(i) == c){
                    if(word.charAt(i) == c){
                        return "-fx-background-color: #6aaa64; -fx-text-fill: white;";
                    }
                    if(word.contains(in)){
                        color = "-fx-background-color: #c9b458; -fx-text-fill: white;";
                    }
                    else if(color.equals("-fx-background-color: #d3d6da; -fx-text-fill: black;")){
                        color = "-fx-background-color: #787c7e; -fx-text-fill: white;";
                    }
                }
            }
        }
        return color;
    }

    public void shakeRow(){
        Parent root = stage.getScene().getRoot();
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), root);

        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setInterpolator(Interpolator.LINEAR);
        shake.play();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            if(stage == null || stage.getScene() == null){
                return;
            }
            Parent root = render();
            stage.getScene().setRoot(root);
            if(model.getAndClearInvalidWord()){
                shakeRow();
            }
        });
    }
}
