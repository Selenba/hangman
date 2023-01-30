package com.game.hangman;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import java.io.*;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    @FXML Label labelAttempts, labelWord;
    @FXML GridPane gridpane;
    @FXML AnchorPane anchorPane;
    @FXML Button buttonReset;
    private String secretWord = "";
    private String displayedWord = "";
    private int errorCounter = 0;
    private static final int MAX_ATTEMPTS = 10;
    private static final Random RANDOM = new Random();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void run() throws IOException {
        generateWord();
        updateLabels();
        createEventHandlers();
    }

    @FXML
    private void reset() {
        labelWord.setTextFill(Color.WHITE);
        errorCounter = 0;
        for (Node n : gridpane.getChildren()){
            n.setVisible(true);
        }
        generateWord();
        updateLabels();
    }

    private void generateWord() {

        Scanner reader = new Scanner(Objects.requireNonNull(getClass().getResourceAsStream("words.txt")));
        List<String> list = new ArrayList<>();

        while(reader.hasNext()){
            list.add(reader.nextLine());
        }

        secretWord = list.get(RANDOM.nextInt(list.size()));
        displayedWord = "_ ".repeat(secretWord.length()).trim();
    }

    private void updateLabels(){
        if(errorCounter < MAX_ATTEMPTS){
            labelAttempts.setText("Tentatives restantes : " + (MAX_ATTEMPTS - errorCounter));
        }else{
            //Game over !
            labelAttempts.setText("La réponse était : " + secretWord);
        }
        labelWord.setText(displayedWord);
    }

    private void createEventHandlers(){
        for (Node n : gridpane.getChildren()){
            n.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onAction);
        }

        anchorPane.addEventHandler(KeyEvent.KEY_TYPED, this::onAction);
    }

    private void onAction(MouseEvent event){
        String input = ((Button)event.getSource()).getText();
        ((Button) event.getSource()).setVisible(false);
        updateWords(input);
        updateLabels();
    }

    private void onAction(KeyEvent event){
        String input = event.getCharacter();

        if(isLetter(input)){
            for (Node n : gridpane.getChildren()){
                if(!n.isVisible()){
                    //If the node isn't visible it means the letter has been tried already
                    //Therefore we just ignore the input
                    event.consume();
                }else if(((Button)n).getText().toLowerCase(Locale.ROOT).equals(input)){
                    //Looks for the button corresponding to the input then processes it
                    n.setVisible(false);
                    updateWords(input.toUpperCase(Locale.ROOT));
                    updateLabels();
                    break;
                }
            }
        }else{
            event.consume();
        }
    }

    private boolean isLetter(String s){
        if (s.length() != 1) {
            return false;
        }
        char c = s.charAt(0);
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private void updateWords(String input){
        char[] secretChars = secretWord.toCharArray();
        char[] displayedChars = displayedWord.toCharArray();
        String temp;

        for (int i = 0; i < secretChars.length; i++) {
            if (secretChars[i] == input.charAt(0)) {
                displayedChars[i * 2] = input.charAt(0);
            }
        }

        temp = new String(displayedChars);

        if(temp.equals(displayedWord)){
            errorCounter++;
            if(errorCounter == MAX_ATTEMPTS){
                displayedWord = "Perdu !";
                labelWord.setTextFill(Color.RED);
                for(Node n : gridpane.getChildren()){
                    n.setVisible(false);
                }
            }
        }else{
            displayedWord = temp;
        }

        if (normalizeDisplayedWord(displayedWord).equals(secretWord)){
            labelWord.setTextFill(Color.GREEN);
            for(Node n : gridpane.getChildren()){
                n.setVisible(false);
            }
        }
    }

    private String normalizeDisplayedWord(String str){
        //Removes the underscores and the spaces from displayedWord before comparison with secretWord
        str = str.replaceAll("_", "");
        char [] chars = str.toCharArray();
        StringBuilder result = new StringBuilder();

        for (char c : chars){
            if(c != ' '){
                result.append(c);
            }
        }
        return result.toString();
    }
}