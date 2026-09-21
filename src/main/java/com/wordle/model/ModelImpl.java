package com.wordle.model;

import java.util.ArrayList;
import java.util.List;

public class ModelImpl implements Model {
    private String word = null;
    private final List<String> guesses;
    private final List<String> grayLetters;
    private final List<Observer> observers;
    private STATUS status;
    private MODE mode;
    private boolean invalidWord = false;
    private int hintIndex = -1;

    public ModelImpl(String word) {
        this.word = word.toUpperCase();
        this.guesses = new ArrayList<>();
        this.grayLetters = new ArrayList<>();
        observers = new ArrayList<>();
        this.status = STATUS.START_GAME;
        this.mode = MODE.EASY;
        hintIndex = -1;
    }

    @Override
    public void startGame(){
        status = STATUS.IN_PROGRESS;
        guesses.clear();
        grayLetters.clear();
        notifyObservers();
    }

    @Override
    public void makeGuess(String guess){
        if(status != STATUS.IN_PROGRESS){
            return;
        }
        guess = guess.toUpperCase();
        if(mode != MODE.HARD){
            for(int i=0; i<guess.length(); i++){
                if(grayLetters.contains(guess.charAt(i))){
                    return;
                }
            }
        }
        guesses.add(guess);
        updateGrayLetters(guess);

        if(guess.equals(word)){
            status = STATUS.END_GAME;
        }
        else if(guesses.size() >= getMaxAttempts()){
            status = STATUS.END_GAME;
        }
        notifyObservers();
    }

    @Override
    public void triggerInvalidWord(){
        this.invalidWord = true;
        notifyObservers();
    }

    @Override
    public boolean getAndClearInvalidWord(){
        boolean current = invalidWord;
        invalidWord = false;
        return current;
    }

    private void updateGrayLetters(String guess){
        for(int i=0; i<guess.length(); i++){
            String letter = String.valueOf(guess.charAt(i));
            if(!word.contains(letter) && !grayLetters.contains(letter)){
                grayLetters.add(letter);
            }
        }
    }

    public int correctLetterCount(){
        boolean[] discoveredLetters = new boolean[5];
        int count = 0;
        for(String guess: guesses){
            for (int i=0; i<5; i++){
                if(guess.charAt(i) == word.charAt(i)){
                    discoveredLetters[i] = true;
                }
            }
        }
        for(boolean bool : discoveredLetters){
            if(bool){
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean canUseHint(){
        if(mode == MODE.HARD || status != STATUS.IN_PROGRESS || hintIndex != -1){
            return false;
        }
        return (guesses.size() > (getMaxAttempts()/2)) && correctLetterCount() <= 2;
    }

    @Override
    public void useHint(){
        if(!canUseHint()){
            return;
        }
       for(int i=0; i<5; i++){
           for(String guess: guesses){
               if(guess.charAt(i) != word.charAt(i)){
                   hintIndex = i;
               }
           }
       }
        notifyObservers();
    }

    @Override
    public int getHintIndex(){
        return hintIndex;
    }

    @Override
    public int getMaxAttempts(){
        if(mode == MODE.EASY){
            return 7;
        }
        else if(mode == MODE.MEDIUM){
            return 6;
        }
        else{
            return 5;
        }
    }

    @Override
    public List<String> getGuesses(){
        return new ArrayList<>(guesses);
    }
    @Override
    public List<String> getGrayLetters(){
        return new ArrayList<>(grayLetters);
    }
    @Override
    public STATUS getStatus(){
        return status;
    }
    @Override
    public void setModeStatus(MODE mode){
        this.mode = mode;
        notifyObservers();
    }
    @Override
    public MODE getModeStatus(){
        return mode;
    }
    public String getTargetWord(){
        return word;
    }

    @Override
    public void setTargetWord(String target){
        word = target.toUpperCase();
        status = STATUS.IN_PROGRESS;
        notifyObservers();
    }
    @Override
    public void resetGame(){
        guesses.clear();
        grayLetters.clear();
        status=STATUS.IN_PROGRESS;
        hintIndex = -1;
        notifyObservers();
    }
    @Override
    public void addObserver(Observer o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    private void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

    public void update(){
        notifyObservers();
    }
}
